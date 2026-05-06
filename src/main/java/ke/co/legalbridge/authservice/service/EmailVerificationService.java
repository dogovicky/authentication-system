package ke.co.legalbridge.authservice.service;

import jakarta.servlet.http.HttpServletRequest;
import ke.co.legalbridge.authservice.dto.emailvalidation.EmailVerificationResponseDTO;
import ke.co.legalbridge.authservice.enumerations.ErrorCode;
import ke.co.legalbridge.authservice.exception.AuthSecurityException;
import ke.co.legalbridge.authservice.exception.BusinessException;
import ke.co.legalbridge.authservice.exception.TechnicalException;
import ke.co.legalbridge.authservice.model.EmailVerificationToken;
import ke.co.legalbridge.authservice.model.User;
import ke.co.legalbridge.authservice.model.UserSession;
import ke.co.legalbridge.authservice.repository.EmailVerificationTokenRepo;
import ke.co.legalbridge.authservice.repository.SessionRepo;
import ke.co.legalbridge.authservice.repository.UserRepo;
import ke.co.legalbridge.authservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final UserRepo userRepo;
    private final EmailVerificationTokenRepo verificationTokenRepo;
    private final JwtService jwtService;
    private final SessionRepo sessionRepo;
    private final RabbitMQPublisher rabbitMQPublisher;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public void requestVerificationEmail(String email) {

        // Check if user exists in the database (After successfully signing up)
        User user = userRepo.findByEmail(email)
                .orElse(null);

        // If user is null, send email notifying user to sign up
        if (user == null) {
            log.warn("A verification email was requested by a user not registered: {}", email);
            throw BusinessException.userNotFound(null, "auth-service");
        }

        // Send email with link
        try {

            String token = generateSecureRandom();

            // Create Verification Token record
            EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                    .userId(user.getId())
                    .token(token)
                    .issuedAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusMinutes(15))
                    .build();

            verificationTokenRepo.save(verificationToken);

            // Build verification link to be sent
            String verificationLink = String.format("%s/verify-email?token=%s", frontendUrl, token);

            // Send email via elixir
            sendVerificationEmail(user, verificationLink);

            log.info("Verification link sent to email: {}, {}", user.getEmail(), token);
        } catch (Exception ex) {
            log.error("Failed to process password reset request: {}", ex.getMessage(), ex);
            throw TechnicalException.externalServiceError(
                    "mail-service",
                    "Failed to send verification email",
                    "auth-service"
            );
        }
    }

    /*
     * Verify token and activate account
     */
    @Transactional
    public EmailVerificationResponseDTO verifyToken(String token, HttpServletRequest request) {

        // Verify token is valid, exists and hasn't expired in the db
        EmailVerificationToken verificationToken = verificationTokenRepo.findByToken(token)
                .orElseThrow(() -> AuthSecurityException.invalidToken("auth-service"));

        // If token exists, check validity
        if (!verificationToken.isValid()) {
            log.warn("Invalid verification token.");
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "Verification link expired. Please request another link.");
        }

        // Get User
        User user = userRepo.findById(verificationToken.getUserId())
                .orElseThrow(() -> BusinessException.userNotFound(verificationToken.getUserId().toString(), "auth-service"));

        // Validate account
        user.setActive(true);
        user.setVerified(true);

        // Clear token from the database after being used.
        verificationTokenRepo.deleteByUserId(user.getId());

        // Generate tokens for auto-login after verification
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Create and track session
        UserSession userSession = UserSession.builder()
                .userId(user.getId())
                .refreshToken(refreshToken)
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .ipAddress(extractIpAddress(request))
                .deviceInfo(extractDeviceInfo(request))
                .build();

        sessionRepo.save(userSession);

        user.setLastLoginAt(LocalDateTime.now()); // Mark first login
        userRepo.save(user);

        log.info("Account verified successfully: {}", user.getEmail());

        // TODO: Publish UserCreatedEvent (to Notification Service, Profile Service)
        // Once verification is successful, send user infor to Profile Service for account updating
//        UserDTO userDTO = new UserDTO(user.getId(), user.getEmail(), user.getUserType().name(), user.isVerified(), user.isActive(), user.getLastLoginAt(), user.getCreatedAt());
//        rabbitMQPublisher.publishMessage(
//                "x.update-profile", "user.profile.update", userDTO);

        return EmailVerificationResponseDTO.builder()
                .success(true)
                .message("Account successfully verified.")
                .email(user.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationInSeconds())
                .sessionId(userSession.getId().toString())
                .build();
    }

    // =============== Helper Methods ===============

    /*
     * Generate token, expires after 45 minutes
     */
    private String generateSecureRandom() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[32]; // 256 bits
        random.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    /*
     * Send verification email with link and token
     */
    public void sendVerificationEmail(User user, String verificationLink) {
        // TODO: Call Elixir Service to send email (Sync Communication)
        rabbitMQPublisher.publishMessage("emailSignup", "emailSignup", verificationLink);
        log.info("A verification email would be sent to {}: {}", user.getEmail(), verificationLink);
    }

    /*
     * Extract IP address from request
     */
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
