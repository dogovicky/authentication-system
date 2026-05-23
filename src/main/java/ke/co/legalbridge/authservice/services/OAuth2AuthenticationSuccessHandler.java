package ke.co.legalbridge.authservice.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ke.co.legalbridge.authservice.exception.BusinessException;
import ke.co.legalbridge.authservice.model.User;
import ke.co.legalbridge.authservice.model.UserSession;
import ke.co.legalbridge.authservice.repository.SessionRepo;
import ke.co.legalbridge.authservice.repository.UserRepo;
import ke.co.legalbridge.authservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepo userRepo;
    private final SessionRepo sessionRepo;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        assert oAuth2User != null;
        String email = (String) oAuth2User.getAttributes().get("email");

        String deviceInfo = extractDeviceInfo(request);
        String ipAddress  = extractIpAddress(request);

        User user = userRepo.findByEmailWithRoles(email)
                .orElseThrow(() -> BusinessException.userNotFound(email, "OAuth2 Service"));


        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        UserSession session = UserSession.builder()
                .userId(user.getId())
                .refreshToken(refreshToken)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .lastUsedAt(LocalDateTime.now())
                .build();

        sessionRepo.save(session);

        String redirect = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        log.info("=============== OAuth2 login successful for: {} =================", email);
        response.sendRedirect(redirect);
    }

    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 500)) : "Unknown";
    }
}
