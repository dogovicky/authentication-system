package ke.co.legalbridge.authservice.dto.events;

import lombok.Builder;

@Builder
public record EmailVerificationEvent(
        String eventId,
        String email,
        String verificationLink
) implements KafkaEvent {
    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public String getEventType() {
        return "email-verification";
    }
}
