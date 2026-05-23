package ke.co.legalbridge.authservice.services.mails;


import ke.co.legalbridge.authservice.dto.events.OtpVerificationEvent;
import ke.co.legalbridge.authservice.services.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class MfaMailService {

    private final MailService mailService;

    @KafkaListener(topics = "${kafka.topics.otp-verification}", groupId = "${kafka.groups.otp-verification}")
    public void sendOtpEmail(ConsumerRecord<String, Object> record) {

        OtpVerificationEvent event = (OtpVerificationEvent) record.value();
        mailService.sendEmail(
                event.email(),
                "Multi-Factor Authentication",
                "mfa-verification",
                Map.of("email", event.email(), "otp", event.otp(), "loginTime",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
        );
    }

}
