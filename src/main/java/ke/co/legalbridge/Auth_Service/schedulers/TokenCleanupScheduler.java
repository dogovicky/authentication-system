package ke.co.legalbridge.Auth_Service.schedulers;

import ke.co.legalbridge.Auth_Service.repository.SessionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final SessionRepo sessionRepo;

    @Value("${app.cleanup.revoked-sessions-retention-days:30}")
    private int revokedSessionRetentionDays;

    @Value("${app.cleanup.expired-sessions-retention-days:7}")
    private int expiredSessionsRetentionDays;

    /*
     * Clean up expired sessions
     * Runs every day and 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredSessions() {
        log.info("Starting cleaning up of expired sessions");

        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(expiredSessionsRetentionDays);

            int deleteCount = sessionRepo.deleteByExpiresAtBeforeAndIsRevokedFalse(cutoffDate);

            log.info("Expired Sessions cleanup complete. Deleted {} sessions that expired before {}", deleteCount, cutoffDate);
        } catch (Exception ex) {
            log.error("Failed to cleanup expired sessions: {}", ex.getMessage(), ex);
        }
    }

    /*
     * Clean up revoked sessions
     * Runs every day Sunday at 3:00 AM
     */
    @Scheduled(cron = "0 0 3 * * SUN")
    @Transactional
    public void cleanupRevokedSessions() {
        log.info("Starting cleaning up of revoked sessions");

        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(revokedSessionRetentionDays);

            int deletedCount = sessionRepo.deleteByRevokedTrueAndRevokedAtBefore(cutoffDate);
            log.info("Revoked sessions cleanup completed. Deleted {} sessions revoked before {}",
                    deletedCount, cutoffDate);
        } catch (Exception ex) {
            log.error("Failed to cleanup revoked sessions: {}", ex.getMessage(), ex);
        }
    }

}
