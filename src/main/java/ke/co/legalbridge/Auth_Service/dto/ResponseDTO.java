package ke.co.legalbridge.Auth_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ResponseDTO {

    private String userId;
    private String email;
    private String userType;
    private boolean isVerified;
    private boolean isActive;

    // Token information
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String sessionId;
    private long expiresIn; // seconds


}
