package ke.co.legalbridge.Auth_Service.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import ke.co.legalbridge.Auth_Service.dto.ResponseDTO;
import ke.co.legalbridge.Auth_Service.dto.SignUpRequestDTO;
import ke.co.legalbridge.Auth_Service.service.RegistrationService;
import ke.co.legalbridge.sharedlibraries.response.ApiResponse;
import ke.co.legalbridge.sharedlibraries.response.ResponseEntityBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Auth related operations")
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<ResponseDTO>> registerNewUser(@Valid @RequestBody SignUpRequestDTO signUpRequestDTO, HttpServletRequest request) {

        long startTime = System.currentTimeMillis();

        ResponseDTO response = registrationService.register(signUpRequestDTO);

        return ResponseEntityBuilder.created(response, "Registration Successful. Please check email to verify account.")
                .withProcessingTime(startTime)
                .withMetadata(
                        request.getHeader("X-Request-ID"),
                        "auth-service",
                        request.getRequestURI()
                )
                .build();
    }

}
