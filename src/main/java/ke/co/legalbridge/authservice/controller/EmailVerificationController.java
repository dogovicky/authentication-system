package ke.co.legalbridge.authservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import ke.co.legalbridge.authservice.dto.emailvalidation.EmailVerificationConfirmDTO;
import ke.co.legalbridge.authservice.dto.emailvalidation.EmailVerificationResponseDTO;
import ke.co.legalbridge.authservice.apiresponse.ApiResponse;
import ke.co.legalbridge.authservice.apiresponse.ResponseEntityBuilder;
import ke.co.legalbridge.authservice.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Email Verification Service", description = "Request email validation link for account activation.")
public class EmailVerificationController {

    private final RegistrationService registrationService;

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<EmailVerificationResponseDTO>> verifyEmail(
            @Valid @RequestBody EmailVerificationConfirmDTO request, HttpServletRequest servletRequest) {

        EmailVerificationResponseDTO response = registrationService.verifyEmail(request.getToken(), servletRequest);

        return ResponseEntityBuilder.ok(response, "Verification Successful").build();
    }

}
