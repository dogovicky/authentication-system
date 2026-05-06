package ke.co.legalbridge.authservice.service;

import jakarta.servlet.http.HttpServletRequest;
import ke.co.legalbridge.authservice.dto.passwordreset.PasswordResetConfirmDTO;
import ke.co.legalbridge.authservice.dto.passwordreset.PasswordResetRequestDTO;
import ke.co.legalbridge.authservice.dto.passwordreset.PasswordResetResponseDTO;
import ke.co.legalbridge.authservice.enumerations.ErrorCode;
import ke.co.legalbridge.authservice.exception.AuthSecurityException;
import ke.co.legalbridge.authservice.exception.BusinessException;
import ke.co.legalbridge.authservice.exception.TechnicalException;
import ke.co.legalbridge.authservice.exception.ValidationException;
import ke.co.legalbridge.authservice.model.PasswordResetToken;
import ke.co.legalbridge.authservice.model.User;
import ke.co.legalbridge.authservice.repository.PasswordResetTokenRepo;
import ke.co.legalbridge.authservice.repository.SessionRepo;
import ke.co.legalbridge.authservice.repository.UserRepo;
import ke.co.legalbridge.authservice.utilities.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepo userRepo;
    private final PasswordResetTokenRepo passwordResetTokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final PasswordUtil passwordUtil = new PasswordUtil();
    private final SessionRepo sessionRepo;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.password-reset.prevent-reuse:true}")
    private boolean preventPasswordReuse;

    /*
     * Step 1: User requests password reset by providing email
     */
    @Transactional
    public PasswordResetResponseDTO requestPasswordReset(PasswordResetRequestDTO request, HttpServletRequest servletRequest) {

        log.info("=========== Password reset requested for email: {} =============", request.getEmail());

        // Find user by email
        User user = userRepo.findByEmail(request.getEmail())
                .orElse(null);

        // SECURITY: Always return success even if user doesn't exist
        // This prevents email enumeration attacks
        if (user == null) {
            log.warn("Password reset requested for non-existent email: {}", request.getEmail());
            return PasswordResetResponseDTO.builder()
                    .success(true)
                    .message("If the email exists, a password reset link has been sent")
                    .build();
        }

        // Check account status
        if (!user.isActive()) {
            log.warn("Password reset requested for inactive account: {}", request.getEmail());
            throw AuthSecurityException.accountLocked("auth-service");
        }

        try {
            // Invalidate any existing tokens for this user
            passwordResetTokenRepo.deleteByUserId(user.getId());

            // Generate secure random token
            String resetToken = generateSecureRandom();

            // Create resetToken record
            PasswordResetToken tokenEntity = PasswordResetToken.builder()
                    .userId(user.getId())
                    .token(resetToken)
                    .issuedAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .ipAddress(extractIpAddress(servletRequest))
                    .build();

            passwordResetTokenRepo.save(tokenEntity);

            // Build reset link for email
            String resetLink = String.format("%s/reset-password?token=%s", frontendUrl, resetToken);

            // Send email via elixir
            sendPasswordResetEmail(user, resetLink, resetToken);

            log.info("Password reset token generated for user: {} and token: {}", user.getEmail(), resetToken);

            return PasswordResetResponseDTO.builder()
                    .email(maskEmail(user.getEmail()))
                    .success(true)
                    .message("A password reset link has been sent to the email.")
                    .build();

        } catch (Exception ex) {
            log.error("Failed to process password reset request: {}", ex.getMessage(), ex);
            throw TechnicalException.externalServiceError(
                    "mail-service",
                    "Failed to process password reset request",
                    "auth-service"
            );
        }
    }

    /*
     * Step 2: Validate reset token (when user clicks link)
     */
    public PasswordResetResponseDTO validateResetToken(String token) {

        log.info("Validating password reset token");

        PasswordResetToken resetToken = passwordResetTokenRepo.findByToken(token)
                .orElseThrow(() -> AuthSecurityException.invalidToken("auth-service"));


        if (!resetToken.isValid()) {
            log.warn("Invalid reset token attempted: expired={}, used={}", resetToken.isExpired(), resetToken.isUsed());
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "Invalid or expired token");
        }

        // Get user to return email
        User user = userRepo.findById(resetToken.getUserId())
                .orElseThrow(() -> BusinessException.userNotFound(resetToken.getUserId().toString(), "auth-service"));

        return PasswordResetResponseDTO.builder()
                .success(true)
                .message("Token is valid. You can now reset your password")
                .email(maskEmail(user.getEmail()))
                .build();
    }

    /*
     * Step 3: Reset password with token and new password
     */
    @Transactional
    public PasswordResetResponseDTO resetPassword(PasswordResetConfirmDTO request, HttpServletRequest servletRequest) {

        log.info("Attempting password reset with token");

        // Validate passwords match
        if (!request.getNewPassword().matches(request.getConfirmPassword())) {
            throw new ValidationException("Passwords do not match", "auth-service");
        }

        // Validate password strength
        PasswordUtil.PasswordValidationResult validationResult = passwordUtil.validationResult(request.getNewPassword());

        if (!validationResult.isValid()) {
            throw ValidationException.passwordTooWeak("auth-service");
        }

        // Find and validate token
        PasswordResetToken resetToken = passwordResetTokenRepo.findByToken(request.getToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN, "Invalid or expired reset token"));

        if (!resetToken.isValid()) {
            log.warn("Attempted to use invalid reset token: expired={}, used={}",
                    resetToken.isExpired(), resetToken.isUsed());
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "Invalid or expired reset token");
        }

        // Get User
        User user = userRepo.findById(resetToken.getUserId())
                .orElseThrow(() -> BusinessException.userNotFound(resetToken.getUserId().toString(), "auth-service"));

        // Check if new password is same as current password
        if (preventPasswordReuse && isPasswordReused(request.getNewPassword(), user.getPasswordHash())) {
            log.warn("User attempted to reuse password: {}", user.getEmail());
            throw new ValidationException(ErrorCode.PASSWORD_TOO_WEAK, "Can't reuse password", "auth-service").addDetail("reason", "Password reuse not allowed");
        }

        try {
            // Update Password
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

            // Reset failed login attempts if account was locked
            user.setFailedLoginAttempts(0);
            user.setLockedAt(null);

            userRepo.save(user);

            // Delete the reset token after use
            passwordResetTokenRepo.deleteByUserId(user.getId());

            // Revoke all sessions after user changes password
            sessionRepo.deleteByUserId(user.getId());

            log.info("Password successfully reset for user: {}", user.getEmail());

            // TODO: Send confirmation email back to user

            return PasswordResetResponseDTO.builder()
                    .success(true)
                    .message("Password has been reset successfully.")
                    .email(maskEmail(user.getEmail()))
                    .build();
        } catch (Exception ex) {
            log.error("Failed to reset password: {}", ex.getMessage(), ex);
            throw TechnicalException.internalServerError("auth-service");
        }

    }

    // ================== Private Helper Methods ================

    /*
     * Check if user is re-using password. Reject password re-using
     * @param newPassword Plain text new password
     * @param currentPasswordHash Hashed current password from database
     * @return true if passwords match (reuse detected)
     */
    private boolean isPasswordReused(String newPassword, String currentPasswordHash) {
        return passwordEncoder.matches(newPassword, currentPasswordHash);
    }

    /*
     * Generate cryptographically secure random token
     */
    private String generateSecureRandom() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[32]; // 256 bits
        random.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
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

    /**
     * Send password reset email via Elixir notification service
     */
    private void sendPasswordResetEmail(User user, String resetLink, String resetToken) {
        // TODO: Call Elixir notification service
        // Example payload:
        // {
        //   "recipient": user.getEmail(),
        //   "template": "password_reset",
        //   "data": {
        //     "name": user.getFirstName(),
        //     "resetLink": resetLink,
        //     "expiryHours": tokenExpiryHours
        //   }
        // }

        log.info("Password reset email would be sent to: {} with link: {}",
                maskEmail(user.getEmail()), resetLink);
    }

    /*
     * Mask email for logging/display (security)
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }

        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() <= 2) {
            return "**@" + domain;
        }

        return localPart.charAt(0) + "***" + localPart.charAt(localPart.length() -1) + "@" + domain;
    }
}
