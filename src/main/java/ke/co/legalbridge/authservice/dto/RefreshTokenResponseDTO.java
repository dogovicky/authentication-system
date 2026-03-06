package ke.co.legalbridge.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;


@Builder
@AllArgsConstructor
public class RefreshTokenResponseDTO {

    private String email;
    private String accessToken;
    private String tokenType;
    private long expiresIn;

}
