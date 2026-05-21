package ke.co.legalbridge.authservice.dto.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record PasswordResetEvent (
        String eventId,
        String email,
        String resetLink
) implements KafkaEvent {

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public String getEventType() {
        return "password-reset";
    }
}
