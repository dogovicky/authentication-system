package ke.co.legalbridge.authservice.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "kafka")
@Data
public class KafkaPropertiesConfig {

    private Map<String, String> topics;
    private Map<String, String> groups;

}
