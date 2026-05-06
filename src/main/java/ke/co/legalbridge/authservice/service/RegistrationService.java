package ke.co.legalbridge.authservice.service;

import ke.co.legalbridge.authservice.dto.ResponseDTO;
import ke.co.legalbridge.authservice.dto.registration.SignUpRequestDTO;
import ke.co.legalbridge.authservice.enumerations.ErrorCode;
import ke.co.legalbridge.authservice.exception.BusinessException;
import ke.co.legalbridge.authservice.exception.TechnicalException;
import ke.co.legalbridge.authservice.mappers.AuthMapper;
import ke.co.legalbridge.authservice.model.Role;
import ke.co.legalbridge.authservice.model.User;
import ke.co.legalbridge.authservice.repository.RoleRepository;
import ke.co.legalbridge.authservice.repository.UserRepo;
import ke.co.legalbridge.authservice.utilities.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final PasswordUtil passwordUtil = new PasswordUtil();
    private final AuthMapper authMapper;
    private final EmailVerificationService verificationService;
    private final RoleRepository roleRepository;


    public ResponseDTO register(SignUpRequestDTO signUpRequestDTO) {

        // Check if User exists in the db
        if (userRepo.existsByEmail(signUpRequestDTO.getEmail())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, signUpRequestDTO.getEmail());
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
            user.setRoles(getDefaultRole());


            // Save user
            User savedUser = userRepo.save(user);
            log.info("============== User registered successfully: {} ==============", savedUser.getEmail());

            // TODO: Send verification email and verify account for user to continue
            verificationService.requestVerificationEmail(savedUser.getEmail());

            // Map to response DTO
            return ResponseDTO.builder()
                    .userId(savedUser.getId().toString())
                    .email(savedUser.getEmail())
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
            throw new BusinessException(ErrorCode.PASSWORD_TOO_WEAK, password);
        }
    }

    // Get default role
    private Set<Role> getDefaultRole() {
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND, "No roles matched your request."));

        return Set.of(userRole);
    }

}
