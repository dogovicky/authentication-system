package ke.co.legalbridge.authservice.dto.events;

import java.util.UUID;

public record AccountDeactivatedEvent(UUID userId) {
}
