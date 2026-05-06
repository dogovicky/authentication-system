package ke.co.legalbridge.authservice.utilities;

import io.jsonwebtoken.*;
import ke.co.legalbridge.authservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtUtil {

    private final JwtService jwtService;
    private String secret;
    private String issuer;


    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(jwtService.getKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException ex){
            log.error("Invalid JWT token format: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("JWT token is expired: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("JWT token is unsupported: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        } catch (Exception ex) {
            log.error("JWT validation error: {}", ex.getMessage());
        }

        return false;
    }

    // ============= CLAIM EXTRACTION ==================
    /*
     * Extract user ID from token
     */
    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    /*
     * Extract email from token
     */
    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    /*
     * Extract roles from token
     */
    public Set<String> extractRoles(String token) {
        return extractClaim(token, claims -> {
            Object roles = claims.get("roles");

            if (!(roles instanceof Collection<?> collection)) {
                return Set.of();
            }

            return collection.stream()
                    .map(Object::toString)
                    .filter(role -> !role.isBlank())
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .collect(Collectors.toUnmodifiableSet());
        });
    }

    /*
     * Extract token type, access or refresh
     */
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtService.getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract issued at date from token
     */
    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }


    /*
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception ex) {
            return true;
        }
    }

}
