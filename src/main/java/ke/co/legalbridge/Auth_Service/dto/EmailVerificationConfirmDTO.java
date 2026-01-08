package ke.co.legalbridge.Auth_Service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class EmailVerificationConfirmDTO {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String token;

}
