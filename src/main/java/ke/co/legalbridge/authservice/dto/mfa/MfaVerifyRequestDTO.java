package ke.co.legalbridge.authservice.dto.mfa;

public record MfaVerifyRequestDTO(
        String mfaSessionId,
        String otp
) {}
