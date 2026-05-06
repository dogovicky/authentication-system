package ke.co.legalbridge.authservice.service;

import ke.co.legalbridge.authservice.dto.DeleteAccountRequestDTO;
import ke.co.legalbridge.authservice.dto.events.AccountDeactivatedEvent;
import ke.co.legalbridge.authservice.exception.AuthSecurityException;
import ke.co.legalbridge.authservice.model.User;
import ke.co.legalbridge.authservice.repository.SessionRepo;
import ke.co.legalbridge.authservice.repository.UserRepo;
import ke.co.legalbridge.authservice.utilities.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteAccountService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final SessionRepo sessionRepo;
    private final ApplicationEventPublisher eventPublisher;

    private final String SERVICE_NAME = "auth-service";

    @Transactional
    public String deactivateAccount(DeleteAccountRequestDTO requestDTO) {
        // Validate user from JWT
        String userId = SecurityContextUtil.getCurrentUserId();
        String email = SecurityContextUtil.getCurrentUserEmail();
        User user = userRepo.findById(UUID.fromString(userId))
                .orElseThrow(() -> AuthSecurityException.forbidden(SERVICE_NAME));

        // Chekc if user is already deactivated
        if (!user.isActive()) {
            throw AuthSecurityException.forbidden(SERVICE_NAME);
        }

        // Verify password matches
        if (!passwordEncoder.matches(requestDTO.getConfirmPassword(), user.getPasswordHash())) {
            throw AuthSecurityException.unauthorized(SERVICE_NAME);
        }

        // If password valid, deactivate account, revoke all sessions and publish message to other listening services
        user.setActive(false);
        user.setVerified(false);
        user.setUpdatedAt(LocalDateTime.now());

        sessionRepo.deleteByUserId(user.getId());
        userRepo.save(user);

        eventPublisher.publishEvent(new AccountDeactivatedEvent(user.getId()));

        return "Account deactivated";
    }

}
