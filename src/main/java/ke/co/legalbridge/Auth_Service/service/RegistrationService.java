package ke.co.legalbridge.Auth_Service.service;

import ke.co.legalbridge.Auth_Service.dto.ResponseTokenDTO;
import ke.co.legalbridge.Auth_Service.dto.SignUpRequestDTO;
import ke.co.legalbridge.Auth_Service.model.User;
import ke.co.legalbridge.Auth_Service.repository.UserRepo;
import ke.co.legalbridge.sharedlibraries.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepo userRepo;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public User register(SignUpRequestDTO signUpRequestDTO) {

        if (userRepo.existsByEmail(signUpRequestDTO.getEmail())) {
            System.out.println("User already exists");
        }

        User user = new User();
        user.setEmail(signUpRequestDTO.getEmail());
        user.setPasswordHash(passwordEncoder.encode(signUpRequestDTO.getPassword()));
        user.setUserType(signUpRequestDTO.getUserType());
        user.setUpdatedAt(LocalDateTime.now());
        user.setCreatedAt(LocalDateTime.now());

        System.out.println("User saved successfully");
        return userRepo.save(user);
    }

}
