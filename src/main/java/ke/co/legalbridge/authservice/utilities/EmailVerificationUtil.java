package ke.co.legalbridge.authservice.utilities;

import ke.co.legalbridge.authservice.model.EmailVerificationToken;
import ke.co.legalbridge.authservice.model.User;
import ke.co.legalbridge.authservice.repository.EmailVerificationTokenRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailVerificationUtil {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    private final EmailVerificationTokenRepo verificationTokenRepo;


    // Returns the email verification link
    /*
     * Build token
     * Save token to db
     * Build link
     * Return link
     */



    @Transactional(propagation = Propagation.REQUIRED)
    public String buildVerificationLink(User user) {

        // Build token
        String token = generateSecureRandom();

        // Save verification token
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .userId(user.getId())
                .token(token)
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15)) // Expires after 15 minutes
                .build();

        verificationTokenRepo.save(verificationToken);

        // build link
        String verificationLink =  String.format("%s/verify-email?token=%s", frontendUrl, token);
        log.info("================ Link build successfully for user: {} ===================", user.getId().toString());

        return verificationLink;
    }

    // ====================== PRIVATE HELPER METHODS ====================
    private String generateSecureRandom() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[32]; // 256 bits
        random.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }


}
