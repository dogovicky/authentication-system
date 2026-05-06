package ke.co.legalbridge.authservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import ke.co.legalbridge.authservice.dto.passwordreset.PasswordResetConfirmDTO;
import ke.co.legalbridge.authservice.dto.passwordreset.PasswordResetRequestDTO;
import ke.co.legalbridge.authservice.dto.passwordreset.PasswordResetResponseDTO;
import ke.co.legalbridge.authservice.dto.passwordreset.PasswordResetValidateDTO;
import ke.co.legalbridge.authservice.service.PasswordResetService;
import ke.co.legalbridge.authservice.apiresponse.ApiResponse;
import ke.co.legalbridge.authservice.apiresponse.ResponseEntityBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/password-reset")
@Tag(name = "Password Reset Service", description = "Handles password reset requests")
public class PasswordResetController {

    private final PasswordResetService resetService;

    /*
     * Step 1: Request password reset (user enters email)
     */
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<PasswordResetResponseDTO>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequestDTO requestDTO, HttpServletRequest servletRequest) {

        PasswordResetResponseDTO response = resetService.requestPasswordReset(requestDTO, servletRequest);

        return ResponseEntityBuilder.ok(response, response.getMessage()).build();
    }

    /*
     * Step 2: Validate reset token (when user clicks email link)
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<PasswordResetResponseDTO>> validateToken(@Valid @RequestBody PasswordResetValidateDTO resetValidateDTO) {
        PasswordResetResponseDTO response = resetService.validateResetToken(resetValidateDTO.getToken());

        return ResponseEntityBuilder.ok(response, response.getMessage()).build();
    }

    /*
     * Step 3: Reset password with token and new password
     */
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<PasswordResetResponseDTO>> resetPassword(
            @Valid @RequestBody PasswordResetConfirmDTO confirmDTO, HttpServletRequest servletRequest) {

        PasswordResetResponseDTO response = resetService.resetPassword(confirmDTO, servletRequest);

        return ResponseEntityBuilder.ok(response, response.getMessage()).build();

    }

}
