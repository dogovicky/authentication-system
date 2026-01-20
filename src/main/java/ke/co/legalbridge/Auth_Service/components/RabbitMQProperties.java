package ke.co.legalbridge.Auth_Service.components;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "spring.rabbitmq")
@Getter
@Setter
public class RabbitMQProperties {

    private Map<String, String> exchanges;
    private Map<String, String> queues;
    private Map<String, String> routingKeys;

}
