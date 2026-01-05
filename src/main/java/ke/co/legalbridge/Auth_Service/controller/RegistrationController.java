package ke.co.legalbridge.Auth_Service.controller;

import jakarta.validation.Valid;
import ke.co.legalbridge.Auth_Service.dto.SignUpRequestDTO;
import ke.co.legalbridge.Auth_Service.model.User;
import ke.co.legalbridge.Auth_Service.service.RegistrationService;
import ke.co.legalbridge.sharedlibraries.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> registerNewUser(@Valid @RequestBody SignUpRequestDTO signUpRequestDTO) {
        User user = registrationService.register(signUpRequestDTO);

        return ResponseEntity.ok(ApiResponse.success(user).message("Sign Up Successful").build());
    }

}
