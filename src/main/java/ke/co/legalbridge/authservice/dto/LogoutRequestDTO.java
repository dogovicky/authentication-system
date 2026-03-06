package ke.co.legalbridge.authservice.dto;

import lombok.Getter;

@Getter
public class LogoutRequestDTO {

    private String email;
    private String refreshToken;

}
