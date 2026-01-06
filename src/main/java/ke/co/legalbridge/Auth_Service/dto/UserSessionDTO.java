package ke.co.legalbridge.Auth_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class UserSessionDTO {

    private String id;
    private String deviceInfo;


}
