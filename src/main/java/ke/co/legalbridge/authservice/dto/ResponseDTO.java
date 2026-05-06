package ke.co.legalbridge.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;


@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ResponseDTO {

    private String userId;
    private String email;
    private boolean isVerified;
    private boolean isActive;

    // Token information
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String sessionId;
    private long expiresIn; // seconds


}
