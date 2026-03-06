package ke.co.legalbridge.authservice.dto;

import lombok.Getter;

@Getter
public class RefreshTokenRequestDTO {

    private String email;
    private String accessToken;
    private String refreshToken;

}
