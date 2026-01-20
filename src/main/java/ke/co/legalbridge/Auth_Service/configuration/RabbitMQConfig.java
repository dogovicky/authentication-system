package ke.co.legalbridge.Auth_Service.configuration;


import ke.co.legalbridge.Auth_Service.components.RabbitMQProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    private final RabbitMQProperties properties;

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    /*
     * Pull connection
     */
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost("/auth");
        connectionFactory.setConnectionTimeout(30000); // 30 seconds

        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        connectionFactory.setPublisherReturns(true);

        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    // Exchanges, Queues and Routing keys configurations
    @Bean
    public Map<String, Exchange> exchanges() {
        return properties.getExchanges().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> new DirectExchange(entry.getValue())));
    }

    @Bean
    public Map<String, Queue> queues() {
        return properties.getQueues().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> new Queue(entry.getValue())));
    }

    @Bean
    public Declarables bindings() {
        return new Declarables(properties.getRoutingKeys().entrySet()
                .stream().map(entry -> {
                    String routingKeyName = entry.getKey();
                    String routingKeyValue = entry.getValue();
                    String queueName = properties.getQueues().get(routingKeyName.split("\\.")[0]);

                    String exchangeName = properties.getExchanges().entrySet()
                            .stream()
                            .filter(e -> routingKeyName.startsWith(e.getKey()))
                            .map(Map.Entry::getValue)
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("No matching exchange for routing key: " + routingKeyName));

                    return BindingBuilder.bind(new Queue(queueName, true))
                            .to(new DirectExchange(exchangeName))
                            .with(routingKeyValue);
                }).toList());
    }


}
