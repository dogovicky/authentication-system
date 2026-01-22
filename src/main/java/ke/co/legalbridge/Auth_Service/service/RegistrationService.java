package ke.co.legalbridge.Auth_Service.service;

import ke.co.legalbridge.Auth_Service.dto.ResponseDTO;
import ke.co.legalbridge.Auth_Service.dto.SignUpRequestDTO;
import ke.co.legalbridge.Auth_Service.exception.InvalidRegistrationException;
import ke.co.legalbridge.Auth_Service.exception.UserAlreadyExistsException;
import ke.co.legalbridge.Auth_Service.exception.WeakPasswordException;
import ke.co.legalbridge.Auth_Service.mappers.AuthMapper;
import ke.co.legalbridge.Auth_Service.model.User;
import ke.co.legalbridge.Auth_Service.repository.UserRepo;
import ke.co.legalbridge.sharedlibraries.dtos.common.UserDTO;
import ke.co.legalbridge.sharedlibraries.exceptions.BusinessException;
import ke.co.legalbridge.sharedlibraries.exceptions.TechnicalException;
import ke.co.legalbridge.sharedlibraries.security.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final PasswordUtil passwordUtil = new PasswordUtil();
    private final AuthMapper authMapper;
    private final EmailVerificationService verificationService;


    public ResponseDTO register(SignUpRequestDTO signUpRequestDTO) {

        // Check if User exists in the db
        if (userRepo.existsByEmail(signUpRequestDTO.getEmail())) {
            throw new UserAlreadyExistsException(signUpRequestDTO.getEmail());
        }

        // Validate User Type
        if (signUpRequestDTO.getUserType() == null) {
            throw InvalidRegistrationException.missingUserType();
        }

        // Validate password strength using password util
        validatePassword(signUpRequestDTO.getPassword());

        try {
            // Map DTO to user entity using map struct
            User user = authMapper.signUpRequestToUser(signUpRequestDTO);

            // Set password hash (MapStruct can't encode passwords)
            user.setPasswordHash(passwordEncoder.encode(signUpRequestDTO.getPassword()));
            user.setUpdatedAt(LocalDateTime.now());
            user.setCreatedAt(LocalDateTime.now());

            // Save user
            User savedUser = userRepo.save(user);
            log.info("============== User registered successfully: {} ==============", savedUser.getEmail());

            // TODO: Send verification email and verify account for user to continue
            verificationService.requestVerificationEmail(savedUser.getEmail());

            // Map to response DTO
            return ResponseDTO.builder()
                    .userId(savedUser.getId().toString())
                    .email(savedUser.getEmail())
                    .userType(savedUser.getUserType().name())
                    .isActive(false)
                    .isVerified(false)
                    .build();
        } catch (Exception ex) {
            log.error("Failed to register user: {}", ex.getMessage(), ex);
            throw TechnicalException.databaseError("auth-service").addDetail("email", signUpRequestDTO.getEmail());
        }
    }

    /**
     * Validate password using shared PasswordUtil
     */
    private void validatePassword(String password) {
        PasswordUtil.PasswordValidationResult validationResult = passwordUtil.validationResult(password);

        if (!validationResult.isValid()) {
            throw WeakPasswordException.fromValidationResult(validationResult);
        }
    }

}
