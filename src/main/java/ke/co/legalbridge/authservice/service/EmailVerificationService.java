package ke.co.legalbridge.authservice.service;

import ke.co.legalbridge.authservice.dto.events.EmailVerificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailVerificationService {

    private final MailService mailService;

    @KafkaListener(topics = "${kafka.topics.email-verification}", groupId = "${kafka.groups.email-verification}")
    public void sendVerificationEmail(ConsumerRecord<String, Object> record) {
        log.info("================= Topic: {}, Partition: {}, offset: {} ===============", record.topic(), record.partition(), record.offset());

        EmailVerificationEvent event = (EmailVerificationEvent) record.value();
        mailService.sendEmail(
                event.email(),
                "Account Verification",
                "email-verification",
                Map.of("email", event.email(), "verificationLink", event.verificationLink())
        );
    }

    public void handleDlt(ConsumerRecord<String, Object> record) {
        log.error("DLT | topic: {} | offset: {} | partition: {} | payload: {}",
                record.topic(), record.offset(), record.partition(), record.value());
    }
}
