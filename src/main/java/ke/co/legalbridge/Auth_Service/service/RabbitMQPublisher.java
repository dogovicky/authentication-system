package ke.co.legalbridge.Auth_Service.service;

import ke.co.legalbridge.Auth_Service.components.RabbitMQProperties;
import ke.co.legalbridge.sharedlibraries.exceptions.TechnicalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties properties;

    public void publishMessage(String exchangeName, String routingKey, Object payload) {
        log.info("Publishing message...: {}, {}", exchangeName, routingKey);

        try {
            String exchange = properties.getExchanges().get(exchangeName);
            String key = properties.getRoutingKeys().get(routingKey);

            log.info("Resolved exchange and key: {}, {}", exchange, key);
            rabbitTemplate.convertAndSend(exchange, key, payload);
            log.info("Message published successfully");
        } catch (Exception e) {
            log.error("Error publishing message: {}", e.getMessage());
            throw new TechnicalException(e.getMessage(), "auth-service");
        }

    }

}
