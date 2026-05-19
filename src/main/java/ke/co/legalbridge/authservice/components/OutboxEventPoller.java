package ke.co.legalbridge.authservice.components;

import ke.co.legalbridge.authservice.configuration.KafkaPropertiesConfig;
import ke.co.legalbridge.authservice.enumerations.OutboxStatus;
import ke.co.legalbridge.authservice.model.OutboxEvent;
import ke.co.legalbridge.authservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxEventPoller {
    /*
     * This event poller will act as the kafka producer
     * Publishing events based on topic saved in the OutboxEvent
     *
     */

    private final OutboxEventRepository eventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaPropertiesConfig propertiesConfig;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void pollAndPublish() {

        log.info("================= Polling for events ================");
        List<OutboxEvent> pendingEvents = eventRepository.findByStatus(OutboxStatus.PENDING);

        for (OutboxEvent event : pendingEvents) {
            try {
                kafkaTemplate.send(
                        propertiesConfig.getTopics().get(event.getEventType()),
                        event.getAggregateId(),
                        event.getPayload());
                log.info("================= Topic published: {} =================", propertiesConfig.getTopics().get(event.getEventType()));
                event.setStatus(OutboxStatus.PUBLISHED);
                event.setPublishedAt(LocalDateTime.now());
                log.info("===================== Event Published: {} ===================", event.getAggregateId());
            }  catch (Exception ex) {
                event.setStatus(OutboxStatus.FAILED);
                log.info("================= Failed to Publish Event: {} ===============", ex.getMessage());
            }
        }
    }

}
