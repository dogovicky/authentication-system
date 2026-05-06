package ke.co.legalbridge.authservice.utilities;

import ke.co.legalbridge.authservice.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SecurityContextUtil {

    /*
     * Get the user ID of currently authenticated user
     * Returns null if no user is authenticated
     *
     * Example: String userId = SecurityContextUtil.getCurrentUserId();
     */
    public static String getCurrentUserId() {
        Authentication authentication = getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        // If using custom UserPrincipal
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getUserId();
        }

        // If using username as principal
        if (principal instanceof String) {
            return (String) principal;
        }

        return null;
    }

    /*
     * Get the email of the current authenticated user
     * Returns null if no user is authenticated
     */
    public static String getCurrentUserEmail() {
        Authentication authentication = getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getEmail();
        }

        return null;
    }

    /*
     * Get all roles of current authenticated User
     * Returns empty set if no user is authenticated
     *
     * Example: Set<String> roles = SecurityContextUtil.getCurrentUserRoles();
     */
    public static Set<String> getCurrentUserRoles() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Set.of();
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).filter(Objects::nonNull)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .collect(Collectors.toSet());
    }

    /*
     * Check if current user has a specific role
     *
     * Example: if (SecurityContextUtil.hasRole("ADMIN")) { ... }
     */
    public static boolean hasRole(String role) {
        Set<String> roles = getCurrentUserRoles();
        return roles.contains(role);
    }

    /*
     * Check if current use has any of the specified roles
     *
     * Example: if (SecurityContextUtil.hasAnyRole("ADMIN", "LAWYER")) { ... }
     */
    public static boolean hasAnyRole(String... roles) {
        Set<String> userRoles = getCurrentUserRoles();
        for (String role : roles) {
            if (userRoles.contains(role)) {
                return true;
            }
        }

        return false;
    }

    /*
     * Check if current user has all the specified roles
     */
    public static boolean hasAllRoles(String... roles) {
        Set<String> userRoles = getCurrentUserRoles();
        for (String role : roles) {
            if (!userRoles.contains(role)) {
                return false;
            }
        }

        return true;
    }

    /*
     * Check if use is authenticated
     *
     * Example: if (SecurityContextUtil.isAuthenticated()) { ... }
     */
    public static boolean isAuthenticated() {
        Authentication authentication = getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

}
