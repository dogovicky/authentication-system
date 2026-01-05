package ke.co.legalbridge.Auth_Service.service;

import ke.co.legalbridge.Auth_Service.model.User;
import ke.co.legalbridge.Auth_Service.repository.UserRepo;
import ke.co.legalbridge.sharedlibraries.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepo userRepo;

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(email)
                .orElseThrow();

        return UserPrincipal.fromDatabase(
                user.getId().toString(),
                user.getEmail(),
                user.getPasswordHash(),
                Set.of(user.getUserType().name()),
                user.getUserType().name(),
                user.isActive() && user.isVerified(),
                user.getLockedAt() != null
        );
    }
}
