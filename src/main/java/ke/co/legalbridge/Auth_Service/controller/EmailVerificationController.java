package ke.co.legalbridge.Auth_Service.controller;

import jakarta.validation.Valid;
import ke.co.legalbridge.Auth_Service.dto.EmailVerificationConfirmDTO;
import ke.co.legalbridge.Auth_Service.dto.EmailVerificationResponseDTO;
import ke.co.legalbridge.Auth_Service.service.EmailVerificationService;
import ke.co.legalbridge.sharedlibraries.response.ApiResponse;
import ke.co.legalbridge.sharedlibraries.response.ResponseEntityBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService verificationService;

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<EmailVerificationResponseDTO>> verifyEmail(@Valid @RequestBody EmailVerificationConfirmDTO request) {

        EmailVerificationResponseDTO response = verificationService.verifyToken(request.getToken());

        return ResponseEntityBuilder.ok(response, response.getMessage()).build();
    }

}
