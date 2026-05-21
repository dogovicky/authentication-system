package ke.co.legalbridge.authservice.dto.events;

import lombok.Builder;

@Builder
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
