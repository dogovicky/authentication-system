package ke.co.legalbridge.authservice.service;

import ke.co.legalbridge.authservice.components.RabbitMQProperties;

import ke.co.legalbridge.authservice.exception.TechnicalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties properties;
    private final ObjectMapper objectMapper;

    //@EventListener(ApplicationReadyEvent.class)
    public void testConnection() {
        log.info("================ Testing rabbitmq connection==============");
        try {
            String payload = "dogo@email.com";
            log.info("Payload as byte: {}", payload);

            rabbitTemplate.convertAndSend("legal_bridge.events", "auth.user_registered", payload);
            log.info("============== Message successfully published ============");
        } catch (Exception ex) {
            log.error("Error occurred while publishing email verification event", ex);
        }
    }

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
