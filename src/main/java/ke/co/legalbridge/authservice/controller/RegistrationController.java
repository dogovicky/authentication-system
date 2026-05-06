package ke.co.legalbridge.authservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import ke.co.legalbridge.authservice.dto.ResponseDTO;
import ke.co.legalbridge.authservice.dto.registration.SignUpRequestDTO;
import ke.co.legalbridge.authservice.service.RegistrationService;
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
@RequestMapping("/api/auth")
@Tag(name = "Registration Service", description = "Handles user registration (All roles)")
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
