package ke.co.legalbridge.authservice.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import ke.co.legalbridge.authservice.configuration.ResendConfig;
import ke.co.legalbridge.authservice.enumerations.ErrorCode;
import ke.co.legalbridge.authservice.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final Resend resend;
    private final ResendConfig resendConfig;
    private final TemplateEngine templateEngine;

    public void sendEmail(String to, String subject, String template, Map<String, Object> variables) {

        try {
            Context context = new Context();
            variables.forEach(context::setVariable);
            String html = templateEngine.process(template, context);
            String text = templateEngine.process(template + "-text", context); //plain text

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(resendConfig.getFrom())
                    .to(to)
                    .subject(subject)
                    .html(html)
                    .text(text) //fallback
                    .build();

            CreateEmailResponse response = resend.emails().send(params);
            log.info("Email sent | template: {} | to: {} | resend id: {}", template, to, response.getId());
        } catch (ResendException e) {
            log.error("============= Error sending email via Resend: {} =================", e.getMessage());
            throw new BusinessException(ErrorCode.EXTERNAL_SERVICE_ERROR, "Failed to send email");
        }

    }

}
