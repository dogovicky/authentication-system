package ke.co.legalbridge.authservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    @KafkaListener(topics = "${kafka.topics.email-verification}", groupId = "${kafka.groups.email-verification}")
    public void sendVerificationEmail(ConsumerRecord<String, Object> record) {

        // Send verification link via email
        /*
         * Build template with thymeleaf
         *
         */


    }

    public void sendWelcomeEmail() {

    }

    /*
     * Verify token and activate account
     */
//    @Transactional
//    public EmailVerificationResponseDTO verifyToken(String token, HttpServletRequest request) {
//
//        // Verify token is valid, exists and hasn't expired in the db
//        EmailVerificationToken verificationToken = verificationTokenRepo.findByToken(token)
//                .orElseThrow(() -> AuthSecurityException.invalidToken("auth-service"));
//
//        // If token exists, check validity
//        if (!verificationToken.isValid()) {
//            log.warn("Invalid verification token.");
//            throw new BusinessException(ErrorCode.INVALID_TOKEN, "Verification link expired. Please request another link.");
//        }
//
//        // Get User
//        User user = userRepo.findById(verificationToken.getUserId())
//                .orElseThrow(() -> BusinessException.userNotFound(verificationToken.getUserId().toString(), "auth-service"));
//
//        // Validate account
//        user.setActive(true);
//        user.setVerified(true);
//
//        // Clear token from the database after being used.
//        verificationTokenRepo.deleteByUserId(user.getId());
//
//        // Generate tokens for auto-login after verification
//        String accessToken = jwtService.generateAccessToken(user);
//        String refreshToken = jwtService.generateRefreshToken(user);
//
//        // Create and track session
//        UserSession userSession = UserSession.builder()
//                .userId(user.getId())
//                .refreshToken(refreshToken)
//                .issuedAt(LocalDateTime.now())
//                .expiresAt(LocalDateTime.now().plusDays(7))
//                .ipAddress(extractIpAddress(request))
//                .deviceInfo(extractDeviceInfo(request))
//                .build();
//
//        sessionRepo.save(userSession);
//
//        user.setLastLoginAt(LocalDateTime.now()); // Mark first login
//        userRepo.save(user);
//
//        log.info("Account verified successfully: {}", user.getEmail());
//
//        // TODO: Publish UserCreatedEvent (to Notification Service, Profile Service)
//        // Once verification is successful, send user infor to Profile Service for account updating
////        UserDTO userDTO = new UserDTO(user.getId(), user.getEmail(), user.getUserType().name(), user.isVerified(), user.isActive(), user.getLastLoginAt(), user.getCreatedAt());
////        rabbitMQPublisher.publishMessage(
////                "x.update-profile", "user.profile.update", userDTO);
//
//        return EmailVerificationResponseDTO.builder()
//                .success(true)
//                .message("Account successfully verified.")
//                .email(user.getEmail())
//                .accessToken(accessToken)
//                .refreshToken(refreshToken)
//                .tokenType("Bearer")
//                .expiresIn(jwtService.getAccessTokenExpirationInSeconds())
//                .sessionId(userSession.getId().toString())
//                .build();
//    }


}
