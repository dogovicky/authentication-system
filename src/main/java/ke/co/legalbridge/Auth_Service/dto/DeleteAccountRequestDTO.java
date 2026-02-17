package ke.co.legalbridge.Auth_Service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteAccountRequestDTO {

    @NotBlank(message = "Please confirm your password.")
    private String confirmPassword;

}
