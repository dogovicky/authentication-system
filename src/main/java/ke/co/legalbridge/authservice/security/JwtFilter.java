package ke.co.legalbridge.authservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ke.co.legalbridge.sharedlibraries.security.JwtUtil;
import ke.co.legalbridge.sharedlibraries.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);

            // Employ JWT util from shared libraries

            if (jwtUtil.validateToken(token)) {

                // Extract claims using JWT util
                String userId = jwtUtil.extractUserId(token);
                String email = jwtUtil.extractEmail(token);
                String userType = jwtUtil.extractUserType(token);
                Set<String> roles = jwtUtil.extractRoles(token);

                // Create UserPrincipal
                UserPrincipal userPrincipal = UserPrincipal.fromToken(userId, email, roles, userType);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userPrincipal, null, userPrincipal.getAuthorities()
                );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authenticated user: {} with roles: {}", email, roles);
            }

        } catch (Exception ex) {
            log.error("JWT authentication failed: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
