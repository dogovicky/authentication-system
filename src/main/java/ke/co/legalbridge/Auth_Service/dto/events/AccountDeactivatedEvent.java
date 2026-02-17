package ke.co.legalbridge.Auth_Service.dto.events;

import java.util.UUID;

public record AccountDeactivatedEvent(UUID userId) {
}
