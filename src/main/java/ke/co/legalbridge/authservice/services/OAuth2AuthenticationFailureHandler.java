package ke.co.legalbridge.authservice.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationFailure(@NonNull HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {

        log.error("!!!!!!!! OAuth2 Authentication failed: {} !!!!!!!!!!", exception.getMessage());

        if (exception.getCause() != null) {
            log.error("Caused by: ", exception.getCause());
        }

        String redirect = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", exception.getMessage())
                .build().toUriString();
        response.sendRedirect(redirect);
    }
}
