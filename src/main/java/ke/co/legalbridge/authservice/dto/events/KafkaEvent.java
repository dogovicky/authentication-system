package ke.co.legalbridge.authservice.dto.events;

public interface KafkaEvent {
    String getEventId();
    String getEventType();
}
