package ke.co.legalbridge.authservice.service;

import jakarta.servlet.http.HttpServletRequest;
import ke.co.legalbridge.authservice.dto.ResponseDTO;
import ke.co.legalbridge.authservice.dto.emailvalidation.EmailVerificationResponseDTO;
import ke.co.legalbridge.authservice.dto.events.EmailVerificationEvent;
import ke.co.legalbridge.authservice.dto.registration.SignUpRequestDTO;
import ke.co.legalbridge.authservice.enumerations.ErrorCode;
import ke.co.legalbridge.authservice.exception.AuthSecurityException;
import ke.co.legalbridge.authservice.exception.BusinessException;
import ke.co.legalbridge.authservice.exception.TechnicalException;
import ke.co.legalbridge.authservice.mappers.AuthMapper;
import ke.co.legalbridge.authservice.model.EmailVerificationToken;
import ke.co.legalbridge.authservice.model.Role;
import ke.co.legalbridge.authservice.model.User;
import ke.co.legalbridge.authservice.model.UserSession;
import ke.co.legalbridge.authservice.repository.EmailVerificationTokenRepo;
import ke.co.legalbridge.authservice.repository.RoleRepository;
import ke.co.legalbridge.authservice.repository.SessionRepo;
import ke.co.legalbridge.authservice.repository.UserRepo;
import ke.co.legalbridge.authservice.security.JwtService;
import ke.co.legalbridge.authservice.utilities.EmailVerificationUtil;
import ke.co.legalbridge.authservice.utilities.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final PasswordUtil passwordUtil = new PasswordUtil();
    private final AuthMapper authMapper;
    private final RoleRepository roleRepository;
    private final EmailVerificationUtil emailVerificationUtil;
    private final OutboxService outboxService;
    private final EmailVerificationTokenRepo verificationTokenRepo;
    private final JwtService jwtService;
    private final SessionRepo sessionRepo;


    @Transactional
    public ResponseDTO register(SignUpRequestDTO signUpRequestDTO) {

        // Check if User exists in the db
        if (userRepo.existsByEmail(signUpRequestDTO.getEmail())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, signUpRequestDTO.getEmail());
        }

        // Validate password strength using password util
        validatePassword(signUpRequestDTO.getPassword());

        try {
            // Map DTO to user entity using map struct
            User user = authMapper.signUpRequestToUser(signUpRequestDTO);

            // Set password hash (MapStruct can't encode passwords)
            user.setPasswordHash(passwordEncoder.encode(signUpRequestDTO.getPassword()));
            user.setUpdatedAt(LocalDateTime.now());
            user.setCreatedAt(LocalDateTime.now());
            user.setRoles(getDefaultRole());


            // Save user
            User savedUser = userRepo.save(user);
            log.info("============== User registered successfully, sending email verification: {} ==============", savedUser.getEmail());

            // Build Link
            String verificationLink = emailVerificationUtil.buildVerificationLink(user);

            // Create an EmailVerificationEvent and save it to outbox events
            EmailVerificationEvent event = buildEmailEvent(user, verificationLink);
            outboxService.saveOutboxEvent(event);

            // Map to response DTO
            return ResponseDTO.builder()
                    .userId(savedUser.getId().toString())
                    .email(savedUser.getEmail())
                    .isActive(false)
                    .isVerified(false)
                    .build();
        } catch (Exception ex) {
            log.error("Failed to register user: {}", ex.getMessage(), ex);
            throw TechnicalException.databaseError("auth-service").addDetail("email", signUpRequestDTO.getEmail());
        }
    }

    public EmailVerificationResponseDTO verifyEmail(String token, HttpServletRequest request) {

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

    // ========================== HELPER METHODS ==========================

    /**
     * Validate password using shared PasswordUtil
     */
    private void validatePassword(String password) {
        PasswordUtil.PasswordValidationResult validationResult = passwordUtil.validationResult(password);

        if (!validationResult.isValid()) {
            throw new BusinessException(ErrorCode.PASSWORD_TOO_WEAK, password);
        }
    }

    // Get default role
    private Set<Role> getDefaultRole() {
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND, "No roles matched your request."));

        return Set.of(userRole);
    }

    private EmailVerificationEvent buildEmailEvent(User user, String verificationLink) {
        return EmailVerificationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .email(user.getEmail())
                .verificationLink(verificationLink)
                .build();
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
