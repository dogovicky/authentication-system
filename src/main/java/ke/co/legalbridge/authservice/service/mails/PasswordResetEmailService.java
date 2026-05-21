package ke.co.legalbridge.authservice.service.mails;

import ke.co.legalbridge.authservice.dto.events.PasswordResetEvent;
import ke.co.legalbridge.authservice.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordResetEmailService {

    private final MailService mailService;

    @KafkaListener(topics = "${kafka.topics.password-reset}", groupId = "${kafka.groups.password-reset}")
    public void sendResetEmail(ConsumerRecord<String, Object> record) {
        log.info("================== Topic: {}, Partition: {}, Offset: {} ====================", record.topic(), record.partition(), record.offset());

        PasswordResetEvent resetEvent = (PasswordResetEvent) record.value();
        mailService.sendEmail(
                resetEvent.email(),
                "Password Reset",
                "password-reset",
                Map.of("email", resetEvent.email(), "resetLink", resetEvent.resetLink())
        );
    }

    @KafkaListener(topics = "${kafka.topics.password-reset-dlt}", groupId = "${kafka.groups.password-reset-dlt}")
    public void handleDlt(ConsumerRecord<String, Object> record) {
        log.error("DLT | topic: {} | offset: {} | partition: {} | payload: {}",
                record.topic(), record.offset(), record.partition(), record.value());
    }


}
