package ke.co.legalbridge.Auth_Service.service;

import jakarta.servlet.http.HttpServletRequest;
import ke.co.legalbridge.Auth_Service.dto.LoginRequestDTO;
import ke.co.legalbridge.Auth_Service.dto.ResponseDTO;
import ke.co.legalbridge.Auth_Service.model.User;
import ke.co.legalbridge.Auth_Service.model.UserSession;
import ke.co.legalbridge.Auth_Service.repository.SessionRepo;
import ke.co.legalbridge.Auth_Service.repository.UserRepo;
import ke.co.legalbridge.sharedlibraries.exceptions.AuthSecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

    private final UserRepo userRepo;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final SessionRepo sessionRepo;

    public ResponseDTO login(LoginRequestDTO loginRequestDTO, HttpServletRequest request) {
        log.info("Attempting login with email: [{}]", loginRequestDTO.getEmail());

        // Find User
        User user = userRepo.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(() -> AuthSecurityException.invalidCredentials("auth-service"));

        // Check account status
        validateAccountStatus(user);

        // Verify password matches
        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPasswordHash())) {
            handleLoginFailed(user);
            throw AuthSecurityException.invalidCredentials("auth-service");
        }

        // Extract Device Info and IPAddress
        String deviceInfo = extractDeviceInfo(request);
        String ipAddress = extractIpAddress(request);

        //Check if session already exists for this device
        UserSession session = sessionRepo.findByUserIdAndDeviceInfoAndIsRevokedFalse(user.getId(), deviceInfo)
                .orElse(null);

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken;

        if (session != null && session.getExpiresAt().isAfter(LocalDateTime.now())) {
            // Reuse existing session - just update it
            session.setLastUsedAt(LocalDateTime.now());
            session.setIpAddress(ipAddress);
            refreshToken = session.getRefreshToken();

            log.info("Reusing existing session for user: {} on device: {}",
                    user.getEmail(), deviceInfo);
        } else {
            // Create a new session only if no valid one exists
            refreshToken = jwtService.generateRefreshToken(user);

            // Create and Track Session
            session = UserSession.builder()
                    .userId(user.getId())
                    .refreshToken(refreshToken)
                    .deviceInfo(extractDeviceInfo(request))
                    .ipAddress(extractIpAddress(request))
                    .issuedAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .lastUsedAt(LocalDateTime.now())
                    .build();

            log.info("Created new session for user: {} on device: {}",
                    user.getEmail(), deviceInfo);
        }

        sessionRepo.save(session);

        // Reset failed attempts on successful login
        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());
        userRepo.save(user);

        log.info("User logged in: {} from IP: {}", user.getEmail(), session.getIpAddress());

        // Build response
        return ResponseDTO.builder()
                .email(user.getEmail())
                .userId(user.getId().toString())
                .userType(user.getUserType().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationInSeconds())
                .sessionId(session.getId().toString())
                .isActive(user.isActive())
                .isVerified(user.isVerified())
                .build();

    }


    private static void validateAccountStatus(User user) {
        // Check if account is locked
        if (user.getLockedAt() != null) {
            throw AuthSecurityException.accountLocked("auth-service");
        }

        // Check if account is verified
        if (!user.isVerified()) {
            throw AuthSecurityException.accountNotVerified("auth-service");
        }

        // Check if account is disabled
        if (!user.isActive()) {
            throw AuthSecurityException.forbidden("auth-service");
        }

    }

    private void handleLoginFailed(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        // Lock account after five failed attempts
        if (attempts >= 5) {
            user.setLockedAt(LocalDateTime.now());
            log.warn("Account locked due to multiple failed login attempts {}", user.getEmail());
        }

        userRepo.save(user);
    }

    private String extractDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 500)) : "Unknown";
    }

    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

}
