package ke.co.legalbridge.Auth_Service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import ke.co.legalbridge.Auth_Service.dto.LoginRequestDTO;
import ke.co.legalbridge.Auth_Service.dto.ResponseDTO;
import ke.co.legalbridge.Auth_Service.service.LoginService;
import ke.co.legalbridge.sharedlibraries.response.ApiResponse;
import ke.co.legalbridge.sharedlibraries.response.ResponseEntityBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication of login requests.")
public class LoginController {

    private final LoginService loginService;

    @Operation(summary = "Login User")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successful Login"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    private ResponseEntity<ApiResponse<ResponseDTO>> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO, HttpServletRequest request) {

        long startTime = System.currentTimeMillis();

        ResponseDTO response = loginService.login(loginRequestDTO, request);

        return ResponseEntityBuilder.accepted(response, "Login Successful")
                .withProcessingTime(startTime)
                .withMetadata(
                        request.getHeader("X-Request-ID"),
                        "auth-service",
                        request.getRequestURI()
                )
                .build();

    }

}
