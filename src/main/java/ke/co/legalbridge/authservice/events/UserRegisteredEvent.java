package ke.co.legalbridge.authservice.events;

import lombok.Builder;

@Builder
public record UserRegisteredEvent(
    String event_id,
    String event_type,
    String source,
    UserPayload payload
) {}
