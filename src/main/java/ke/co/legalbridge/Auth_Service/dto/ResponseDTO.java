package ke.co.legalbridge.Auth_Service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder

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
    private long expiresIn; // seconds


}
