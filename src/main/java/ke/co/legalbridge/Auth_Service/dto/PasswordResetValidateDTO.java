package ke.co.legalbridge.Auth_Service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PasswordResetValidateDTO {

    @NotBlank(message = "Reset Token is required")
    private String token;

}
