package ke.co.legalbridge.authservice.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "resend")
@Data
public class ResendConfig {

    private String apiKey;
    private String from;

}
