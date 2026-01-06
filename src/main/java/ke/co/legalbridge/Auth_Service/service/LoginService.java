package ke.co.legalbridge.Auth_Service.service;

import ke.co.legalbridge.Auth_Service.dto.LoginRequestDTO;
import ke.co.legalbridge.Auth_Service.dto.ResponseDTO;
import ke.co.legalbridge.Auth_Service.model.User;
import ke.co.legalbridge.Auth_Service.repository.UserRepo;
import ke.co.legalbridge.sharedlibraries.exceptions.AuthSecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

    private final UserRepo userRepo;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public ResponseDTO login(LoginRequestDTO loginRequestDTO) {

        // Find User
        User user = userRepo.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(() -> AuthSecurityException.invalidCredentials("auth-service"));

        // Check account status
        //validateAccountStatus(user);

        // Verify password matches
        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPasswordHash())) {
            handleLoginFailed(user);
            throw AuthSecurityException.invalidCredentials("auth-service");
        }

        // Reset failed attempts on successful login
        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());
        userRepo.save(user);

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Build response
        return ResponseDTO.builder()
                .email(user.getEmail())
                .userId(user.getId().toString())
                .userType(user.getUserType().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationInSeconds())
                .build();

    }


    private void validateAccountStatus(User user) {
        // Check if account is locked
        if (user.getLockedAt() != null) {
            throw AuthSecurityException.accountLocked("auth-service");
        }

        // Check if account is verified
        if (!user.isVerified()) {
            throw AuthSecurityException.accountNotVerified("auth-service");
        }

        // Check if account is disabled
        if (!user.isActive()) {
            throw AuthSecurityException.forbidden("auth-service");
        }

    }

    private void handleLoginFailed(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        // Lock account after five failed attempts
        if (attempts >= 5) {
            user.setLockedAt(LocalDateTime.now());
            log.warn("Account locked due to multiple failed login attempts {}", user.getEmail());
        }

        userRepo.save(user);
    }

}
