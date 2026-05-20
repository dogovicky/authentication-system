package ke.co.legalbridge.authservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.util.JsonFormat;
import ke.co.legalbridge.authservice.dto.events.KafkaEvent;
import ke.co.legalbridge.authservice.enumerations.ErrorCode;
import ke.co.legalbridge.authservice.enumerations.OutboxStatus;
import ke.co.legalbridge.authservice.exception.BusinessException;
import ke.co.legalbridge.authservice.model.OutboxEvent;
import ke.co.legalbridge.authservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxEventRepository eventRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.MANDATORY) // Fails if no active transaction
    public void saveOutboxEvent(KafkaEvent event) {
        try {
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateId(event.getEventId())
                    .aggregateType(event.getClass().getName())
                    .eventType(event.getEventType())
                    .payload(objectMapper.writeValueAsString(event)) // Serialize event
                    .topic(event.getEventType())
                    .status(OutboxStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();
            eventRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            log.error("============== Error building event: {} ===============", e.getMessage());
            throw new BusinessException(ErrorCode.EXTERNAL_SERVICE_ERROR, "Failed to save outbox event");
        }
    }


}
