package ke.co.legalbridge.authservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import ke.co.legalbridge.authservice.apiresponse.ApiResponse;
import ke.co.legalbridge.authservice.apiresponse.ResponseEntityBuilder;
import ke.co.legalbridge.authservice.dto.ResponseDTO;
import ke.co.legalbridge.authservice.dto.mfa.MfaVerifyRequestDTO;
import ke.co.legalbridge.authservice.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/mfa")
@RequiredArgsConstructor
@Tag(name = "MFA Verification", description = "Handles MFA api endpoints")
public class MfaVerificationController {

    private final LoginService loginService;

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify User identity")
    public ResponseEntity<ApiResponse<ResponseDTO>> verifyOtp(@RequestBody MfaVerifyRequestDTO requestDTO, HttpServletRequest request) {
        return ResponseEntityBuilder.ok(loginService.verifyMfa(requestDTO, request)).build();
    }

}
