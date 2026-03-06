package ke.co.legalbridge.authservice.listeners;

import ke.co.legalbridge.authservice.dto.events.AccountDeactivatedEvent;
import ke.co.legalbridge.authservice.service.RabbitMQPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AccountEventListener {

    private final RabbitMQPublisher  rabbitMQPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAccountDeactivated(AccountDeactivatedEvent event) {

        rabbitMQPublisher.publishMessage(
                "accountDeactivation",
                "accountDeactivation",
                event.userId()
        );

    }

}
