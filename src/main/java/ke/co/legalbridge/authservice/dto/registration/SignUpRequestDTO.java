package ke.co.legalbridge.authservice.dto.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SignUpRequestDTO {

    @Email(message = "Please enter a valid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Create password")
//    @Pattern(regexp = "[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]")
    private String password;

}
