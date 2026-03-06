package ke.co.legalbridge.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
public class UserSessionDTO {

    private String id;
    private String deviceInfo;
    private String ipAddress;
    private LocalDateTime issuedAt;
    private LocalDateTime lastUsedAt;
    private LocalDateTime expiresAt;

}
