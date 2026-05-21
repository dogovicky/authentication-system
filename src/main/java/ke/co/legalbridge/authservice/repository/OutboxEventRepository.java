package ke.co.legalbridge.authservice.repository;

import ke.co.legalbridge.authservice.enumerations.OutboxStatus;
import ke.co.legalbridge.authservice.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findByStatus(OutboxStatus status);

    // Pick up pending + failed events
    @Query("SELECT o FROM OutboxEvent o WHERE o.status = 'FAILED' AND o.attempts < 3 ORDER BY o.createdAt ASC")
    List<OutboxEvent> findRetryable();

    @Query("SELECT o FROM OutboxEvent o WHERE o.status = 'PENDING' ORDER BY o.createdAt ASC")
    List<OutboxEvent> findPendingByAscOrder();

}
