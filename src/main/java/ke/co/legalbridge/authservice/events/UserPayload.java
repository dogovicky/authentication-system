package ke.co.legalbridge.authservice.events;

public record UserPayload(
        String user_id,
        String email
) {}
