package ke.co.legalbridge.authservice.dto.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequestDTO implements Serializable {

    @Email(message = "Please enter a valid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Create password")
//    @Pattern(regexp = "[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]")
    private String password;

}
