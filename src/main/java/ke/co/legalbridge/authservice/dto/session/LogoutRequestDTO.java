package ke.co.legalbridge.authservice.dto.session;

import lombok.Getter;

@Getter
public class LogoutRequestDTO {

    private String email;
    private String refreshToken;

}
