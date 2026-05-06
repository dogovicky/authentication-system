package ke.co.legalbridge.authservice.dto.passwordreset;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PasswordResetValidateDTO {

    @NotBlank(message = "Reset Token is required")
    private String token;

}
