package ke.co.legalbridge.Auth_Service.dto;

import lombok.Getter;

@Getter
public class RefreshTokenRequestDTO {

    private String email;
    private String accessToken;
    private String refreshToken;

}
