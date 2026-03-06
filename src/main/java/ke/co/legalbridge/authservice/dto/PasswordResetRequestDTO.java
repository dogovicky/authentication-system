package ke.co.legalbridge.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PasswordResetRequestDTO {

    @Email(message = "Please enter a valid email address")
    @NotBlank
    private String email;

}
