package ke.co.legalbridge.authservice.components;

import ke.co.legalbridge.authservice.model.OutboxEvent;
import ke.co.legalbridge.authservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxEventPoller {

    private final OutboxEventRepository eventRepository;
    private final OutboxEventPublisher eventPublisher;

    @Scheduled(fixedDelay = 5000)
    public void pollPending() {
        log.info("================= Polling pending events ================");
        List<OutboxEvent> pendingEvents = eventRepository.findPendingByAscOrder();
        pendingEvents.forEach(eventPublisher::publish);
    }

    @Scheduled(fixedDelay = 60000) // Every 60 seconds
    public void pollFailed() {
        log.info("================= Polling failed events ====================");
        List<OutboxEvent> failedEvents = eventRepository.findRetryable();
        if (!failedEvents.isEmpty()) {
            log.warn("!!!!!! Retrying {} failed outbox events !!!!!!", failedEvents.size());
            failedEvents.forEach(eventPublisher::publish);
        }
    }



}
