package ke.co.legalbridge.authservice.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import ke.co.legalbridge.authservice.configuration.KafkaPropertiesConfig;
import ke.co.legalbridge.authservice.enumerations.OutboxStatus;
import ke.co.legalbridge.authservice.model.OutboxEvent;
import ke.co.legalbridge.authservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final OutboxEventRepository eventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaPropertiesConfig propertiesConfig;
    private final ObjectMapper objectMapper;

    private static final int MAX_ATTEMPTS = 3;

    public void publish(OutboxEvent event) {
        try {
            Class<?> eventClass = Class.forName(event.getAggregateType());
            Object eventObject = objectMapper.readValue(event.getPayload(), eventClass);

            kafkaTemplate.send(
                    propertiesConfig.getTopics().get(event.getEventType()),
                    event.getAggregateId(),
                    eventObject);
            log.info("================= Topic published: {} =================", propertiesConfig.getTopics().get(event.getEventType()));
            event.setStatus(OutboxStatus.PUBLISHED);
            event.setPublishedAt(LocalDateTime.now());
            log.info("===================== Event Published: {} ===================", event.getAggregateId());
        } catch (Exception ex) {
            event.setAttempts(event.getAttempts() + 1);
            event.setLastError(ex.getMessage());
            event.setStatus(OutboxStatus.FAILED);

            if (event.getAttempts() >= MAX_ATTEMPTS) {
                log.error("Event permanently failed | id: {} | attempts: {}/{} | error: {}",
                        event.getId(), event.getAttempts(), MAX_ATTEMPTS, ex.getMessage());
                // alert
            } else {
                log.warn("Event failed, queued for retry | id: {} | attempt: {}/{} | error: {}",
                        event.getId(), event.getAttempts(), MAX_ATTEMPTS, ex.getMessage());
            }
        } finally {
            eventRepository.save(event);
        }
    }

}
