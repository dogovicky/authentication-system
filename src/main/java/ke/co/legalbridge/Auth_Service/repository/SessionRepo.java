package ke.co.legalbridge.Auth_Service.repository;

import ke.co.legalbridge.Auth_Service.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepo extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findByRefreshToken(String refreshToken);

    List<UserSession> findByUserIdAndIsRevokedFalse(UUID userId);

    List<UserSession> findByUserId(UUID userId);

    // Clean up expired sessions
    void deleteByExpiresAtBefore(LocalDateTime dateTime);

    // Find active sessions for a user
    List<UserSession> findByUserIdAndIsRevokedFalseAndExpiresAtAfter(UUID userId, LocalDateTime now);
}
