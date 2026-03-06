package ke.co.legalbridge.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteAccountRequestDTO {

    @NotBlank(message = "Please confirm your password.")
    private String confirmPassword;

}
