package ke.co.legalbridge.authservice.service;

import jakarta.servlet.http.HttpServletRequest;
import ke.co.legalbridge.authservice.dto.events.OtpVerificationEvent;
import ke.co.legalbridge.authservice.dto.login.LoginRequestDTO;
import ke.co.legalbridge.authservice.dto.ResponseDTO;
import ke.co.legalbridge.authservice.dto.mfa.MfaVerifyRequestDTO;
import ke.co.legalbridge.authservice.enumerations.MfaStatus;
import ke.co.legalbridge.authservice.exception.AuthSecurityException;
import ke.co.legalbridge.authservice.model.MfaToken;
import ke.co.legalbridge.authservice.model.User;
import ke.co.legalbridge.authservice.model.UserSession;
import ke.co.legalbridge.authservice.repository.MfaTokenRepository;
import ke.co.legalbridge.authservice.repository.SessionRepo;
import ke.co.legalbridge.authservice.repository.UserRepo;
import ke.co.legalbridge.authservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

    private final UserRepo userRepo;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final SessionRepo sessionRepo;
    private final MfaTokenRepository tokenRepository;
    private final OutboxService outboxService;

    @Transactional
    public ResponseDTO login(LoginRequestDTO loginRequestDTO, HttpServletRequest servletRequest) {
        // Find User
        User user = userRepo.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(() -> AuthSecurityException.invalidCredentials("auth-service"));

        validateAccountStatus(user);

        // Verify password matches
        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPasswordHash())) {
            handleLoginFailed(user);
            throw AuthSecurityException.invalidCredentials("auth-service");
        }

        String deviceInfo = extractDeviceInfo(servletRequest);
        String ipAddress = extractIpAddress(servletRequest);

        // Reset Failed attempts
        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());
        userRepo.save(user);

        // Known device check
        boolean knownDevice = sessionRepo
                .existsByUserIdAndDeviceInfoAndRevokedFalseAndExpiresAtAfter(user.getId(), deviceInfo, LocalDateTime.now());

        if (!knownDevice) {
            // Trigger MFA
            return triggerMfa(user, deviceInfo, ipAddress);
        }

        // Known device
        return issueTokens(user, deviceInfo, ipAddress);
    }

    // Trigger MFA via email and OTP
    private ResponseDTO triggerMfa(User user, String deviceInfo, String ipAddress) {
        String otp = generateOtp();

        MfaToken token = MfaToken.builder()
                .otp(otp)
                .MfaStatus(MfaStatus.PENDING)
                .ipAddress(ipAddress)
                .deviceInfo(deviceInfo)
                .userId(user.getId())
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        tokenRepository.save(token);

        // Fire OTP event via Kafka outbox
        OtpVerificationEvent event = OtpVerificationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .otp(otp)
                .email(user.getEmail())
                .build();

        outboxService.saveOutboxEvent(event);

        return ResponseDTO.builder()
                .mfaRequired(true)
                .mfaSessionId(token.getId().toString())
                .message("New device detected. Enter the OTP sent to your email.")
                .build();
    }

    // Verify OTP
    public ResponseDTO verifyMfa(MfaVerifyRequestDTO requestDTO, HttpServletRequest request) {
        // Find the token with session id
        MfaToken token = tokenRepository.findById(UUID.fromString(requestDTO.mfaSessionId()))
                .orElseThrow(() -> AuthSecurityException.invalidToken("auth-service"));

        // Validate
        if (token.getMfaStatus() != MfaStatus.PENDING) {
            throw AuthSecurityException.invalidToken("auth-service");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            token.setMfaStatus(MfaStatus.EXPIRED);
            tokenRepository.save(token);
            throw AuthSecurityException.tokenExpired("auth-service");
        }

        if (!token.getOtp().equals(requestDTO.otp())) {
            throw AuthSecurityException.invalidToken("auth-service");
        }

        // Mark verified
        token.setMfaStatus(MfaStatus.VERIFIED);
        tokenRepository.save(token);

        User user = userRepo.findById(token.getUserId())
                .orElseThrow(() -> AuthSecurityException.invalidCredentials("auth-service"));

        return issueTokens(user, token.getDeviceInfo(), token.getIpAddress());
    }

    private ResponseDTO issueTokens(User user, String deviceInfo, String ipAddress) {
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
        return ResponseDTO.builder()
                .email(user.getEmail())
                .userId(user.getId().toString())
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

    private String generateOtp() {
        return String.valueOf(100000 + new SecureRandom().nextInt(900000)); // 6 digit otp
    }

}
