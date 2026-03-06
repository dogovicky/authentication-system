package ke.co.legalbridge.authservice.repository;

import ke.co.legalbridge.authservice.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepo extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUserIdAndUsedFalseAndExpiresAtAfter(UUID userId, LocalDateTime now);

    // Delete expired tokens
    void deleteByExpiresAtBefore(LocalDateTime dateTime);

    // Invalidate all previous tokens for a user
    void deleteByUserId(UUID userId);

}
