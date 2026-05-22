package ke.co.legalbridge.authservice.dto.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record OtpVerificationEvent (
        String eventId,
        String otp,
        String email
)
implements KafkaEvent {

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public String getEventType() {
        return "otp-verification";
    }
}
