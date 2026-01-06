package ke.co.legalbridge.Auth_Service.dto;

import lombok.Getter;

@Getter
public class LogoutRequestDTO {

    private String email;
    private String refreshToken;

}
