package ke.co.legalbridge.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PasswordResetResponseDTO {

    private String message;
    private boolean success;
    private String email;

}
