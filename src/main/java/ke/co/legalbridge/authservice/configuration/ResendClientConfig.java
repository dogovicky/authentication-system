package ke.co.legalbridge.authservice.configuration;

import com.resend.Resend;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ResendClientConfig {

    private final ResendConfig resendConfig;

    @Bean
    public Resend resendClient() {
        return new Resend(resendConfig.getApiKey());
    }

}
