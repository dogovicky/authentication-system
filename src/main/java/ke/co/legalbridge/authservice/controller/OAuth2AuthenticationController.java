package ke.co.legalbridge.authservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "OAuth2 Authentication", description = "Handles OAuth2 logins and sign ups")
public class OAuth2AuthenticationController {

    @GetMapping("/providers")
    public ResponseEntity<Map<String, String>> getProviders() {
        Map<String, String> providers = Map.of(
                "google", "/oauth2/authorize/google",
                "github", "/oauth2/authorize/github"
        );
        return ResponseEntity.ok(providers);
    }


}
