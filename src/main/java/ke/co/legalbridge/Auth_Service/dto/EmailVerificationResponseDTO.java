package ke.co.legalbridge.Auth_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailVerificationResponseDTO {

    private boolean success;
    private String message;
    private String email;

    // Tokens for auto-login after verification
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String sessionId;
    private Long expiresIn;

}
