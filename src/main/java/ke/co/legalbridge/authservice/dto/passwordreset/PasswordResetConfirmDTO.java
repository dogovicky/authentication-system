package ke.co.legalbridge.authservice.dto.passwordreset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PasswordResetConfirmDTO {

    @NotBlank(message = "Reset Token is required")
    private String token;

    @NotBlank(message = "New Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 an 128 characters")
    private String newPassword;

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;

}
