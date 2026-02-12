package ke.co.legalbridge.Auth_Service.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import ke.co.legalbridge.Auth_Service.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.issuer:legal-bridge}")
    private String issuer;

    @Value("${jwt.access-token-expiration:3600000}") // 60 minutes
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800000}") // 7 days
    private long refreshTokenExpiration;


    public SecretKey getKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(User user) {

        Map<String, Object> claims = new HashMap<>();

        Set<String> roles = Set.of(user.getUserType().name());
        claims.put("roles", roles);
        claims.put("userId", user.getId().toString());
        claims.put("email", user.getEmail());
        claims.put("userType", user.getUserType().name());
        claims.put("type", "access"); // Access Token type


        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration)) // Expires after one hour
                .signWith(getKey())
                .compact();

    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("type", "refresh");

        return Jwts.builder()
                .claims(claims)
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getKey())
                .compact();
    }

    public long getAccessTokenExpirationInSeconds() {
        return accessTokenExpiration / 1000;
    }

    public long getRefreshTokenExpirationInSeconds() {
        return refreshTokenExpiration / 1000;
    }

}
