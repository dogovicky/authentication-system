package ke.co.legalbridge.authservice.service;

import jakarta.servlet.http.HttpServletRequest;
import ke.co.legalbridge.authservice.dto.RefreshTokenResponseDTO;
import ke.co.legalbridge.authservice.dto.UserSessionDTO;
import ke.co.legalbridge.authservice.model.User;
import ke.co.legalbridge.authservice.model.UserSession;
import ke.co.legalbridge.authservice.repository.SessionRepo;
import ke.co.legalbridge.authservice.repository.UserRepo;
import ke.co.legalbridge.sharedlibraries.exceptions.AuthSecurityException;
import ke.co.legalbridge.sharedlibraries.security.JwtUtil;
import ke.co.legalbridge.sharedlibraries.security.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final JwtService jwtService;
    private final JwtUtil jwtUtil;
    private final SessionRepo sessionRepo;
    private final UserRepo userRepo;


    public RefreshTokenResponseDTO refreshAccessToken(String refreshToken, HttpServletRequest request) {

        // Validate refresh token (JWT Signature)
        if (!jwtUtil.validateToken(refreshToken)) {
            throw AuthSecurityException.invalidToken("auth-service");
        }

        // Check if session exists and is valid
        UserSession session = sessionRepo.findByRefreshToken(refreshToken)
                .orElseThrow(() -> AuthSecurityException.invalidToken("auth-service"));

        if (session.isRevoked()) {
            throw AuthSecurityException.sessionExpired("auth-service");
        }

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw AuthSecurityException.sessionExpired("auth-service");
        }

        // Get User
        User user = userRepo.findById(session.getUserId())
                .orElseThrow(() -> AuthSecurityException.invalidToken("auth-service"));

        // Check Account Status
        //verificationStatus(user);

        // Generate new access token
        String newAccessToken = jwtService.generateAccessToken(user);

        // Update user session
        session.setLastUsedAt(LocalDateTime.now());
        sessionRepo.save(session);

        log.info("Access token refreshed for user: {}", user.getEmail());

        return RefreshTokenResponseDTO.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationInSeconds())
                .build();
    }

    public void logout(String refreshToken) {
        sessionRepo.findByRefreshToken(refreshToken)
                .ifPresent(session -> {
                    session.setRevoked(true);
                    session.setRevokedAt(LocalDateTime.now());
                    sessionRepo.save(session);
                    log.info("User logged out, session revoked: {}", session.getId());
                });
    }

    // No userId parameter needed - get from SecurityContext
    public void logoutSession(String sessionId) {
        String userId = SecurityContextUtil.getCurrentUserId();

        if (userId == null) {
            throw AuthSecurityException.unauthorized("auth-service");
        }

        UserSession session = sessionRepo.findById(UUID.fromString(sessionId))
                .orElseThrow(() -> AuthSecurityException.sessionExpired("auth-service"));

        // ✅ Security: Ensure session belongs to requesting user
        if (!session.getUserId().toString().equals(userId)) {
            throw AuthSecurityException.forbidden("auth-service");
        }

        session.setRevoked(true);
        session.setRevokedAt(LocalDateTime.now());
        sessionRepo.save(session);

        log.info("Session {} logged out by user: {}", sessionId, userId);
    }

    public void logoutAllSessions() {
        String userId = SecurityContextUtil.getCurrentUserId(); // Get context from authentication

        if (userId == null) {
            throw AuthSecurityException.unauthorized("auth-service");
        }

        List<UserSession> sessions = sessionRepo.findByUserIdAndIsRevokedFalse(UUID.fromString(userId));
        sessions.forEach(session -> {
            session.setRevoked(true);
            session.setRevokedAt(LocalDateTime.now());
        });

        sessionRepo.saveAll(sessions);
        log.info("All sessions revoked for user: {}", userId);
    }

    public List<UserSessionDTO> getActiveSessions() {

        String userId = SecurityContextUtil.getCurrentUserId();

        if (userId == null) {
            throw AuthSecurityException.unauthorized("auth-service");
        }

        List<UserSession> sessions = sessionRepo
                .findByUserIdAndIsRevokedFalseAndExpiresAtAfter(UUID.fromString(userId), LocalDateTime.now());

        if (sessions.isEmpty()) {
            return new ArrayList<>();
        }

        return sessions.stream()
                .map(session -> UserSessionDTO.builder()
                        .id(session.getId().toString())
                        .deviceInfo(session.getDeviceInfo())
                        .ipAddress(session.getIpAddress())
                        .issuedAt(session.getIssuedAt())
                        .lastUsedAt(session.getLastUsedAt())
                        .expiresAt(session.getExpiresAt())
                        .build())
                .collect(Collectors.toList());
    }

}
