package ke.co.legalbridge.Auth_Service.service;

import jakarta.servlet.http.HttpServletRequest;
import ke.co.legalbridge.Auth_Service.dto.RefreshTokenResponseDTO;
import ke.co.legalbridge.Auth_Service.dto.UserSessionDTO;
import ke.co.legalbridge.Auth_Service.model.User;
import ke.co.legalbridge.Auth_Service.model.UserSession;
import ke.co.legalbridge.Auth_Service.repository.SessionRepo;
import ke.co.legalbridge.Auth_Service.repository.UserRepo;
import ke.co.legalbridge.sharedlibraries.exceptions.AuthSecurityException;
import ke.co.legalbridge.sharedlibraries.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public void logoutAllSessions(String userId) {
        List<UserSession> sessions = sessionRepo.findByUserIdAndIsRevokedFalse(UUID.fromString(userId));
        sessions.forEach(session -> {
            session.setRevoked(true);
            session.setRevokedAt(LocalDateTime.now());
        });
        sessionRepo.saveAll(sessions);
        log.info("All sessions revoked for user: {}", userId);
    }

    public List<UserSessionDTO> getActiveSessions(String userId) {
        List<UserSession> sessions = sessionRepo.findByUserIdAndIsRevokedFalseAndExpiresAtAfter(UUID.fromString(userId), LocalDateTime.now());

        if (sessions.isEmpty()) {
            return new ArrayList<>();
        }

        List<UserSessionDTO> activeSessions = new ArrayList<>();
        for (UserSession session : sessions) {
            UserSessionDTO userSessionDTO = UserSessionDTO.builder()
                    .id(session.getUserId().toString())
                    .deviceInfo(session.getDeviceInfo())
                    .build();
            activeSessions.add(userSessionDTO);
        }
        return activeSessions;
    }

}
