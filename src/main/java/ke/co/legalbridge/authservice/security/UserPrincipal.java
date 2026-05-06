package ke.co.legalbridge.authservice.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPrincipal implements UserDetails {

    private String userId;
    private String email;
    private String password; // Hashed password (not sent in JWT)
    private Collection<? extends GrantedAuthority> authorities;
    private boolean enabled;
    private boolean accountNonLocked; // Return true if account is not locked
    private boolean accountNonExpired; // Returns true if account is not expired
    private boolean credentialsNonExpired; // Returns true if credentials are not expired

    // For storing additional JWT claims
    private Map<String, Object> claims;

    public static UserPrincipal fromToken(String userId, String email, Collection<? extends GrantedAuthority> authorities) {
        return UserPrincipal.builder()
                .userId(userId)
                .email(email)
                .authorities(authorities)
                .enabled(true)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .claims(new HashMap<>())
                .build();
    }

    public static UserPrincipal fromDatabase(String userId, String email, String hashedPassword,
                                             Collection<? extends GrantedAuthority> authorities, boolean enabled, boolean accountLocked) {

        return UserPrincipal.builder()
                .userId(userId)
                .email(email)
                .password(hashedPassword)
                .authorities(authorities)
                .enabled(enabled)
                .accountNonLocked(!accountLocked)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .claims(new HashMap<>())
                .build();

    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @NullMarked
    @Override
    public String getUsername() {
        return email; // Use email as username
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
