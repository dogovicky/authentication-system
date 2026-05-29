package ke.co.legalbridge.authservice.dto.login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class LoginRequestDTO {

    @NotBlank(message = "Please enter an email address.")
    @Email(message = "Please enter a valid email")
    private String email;

    @NotBlank(message = "Please enter your password")
    private String password;


}
