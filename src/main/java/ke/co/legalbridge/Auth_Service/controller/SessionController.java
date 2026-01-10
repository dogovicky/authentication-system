package ke.co.legalbridge.Auth_Service.controller;

import jakarta.servlet.http.HttpServletRequest;
import ke.co.legalbridge.Auth_Service.dto.LogoutRequestDTO;
import ke.co.legalbridge.Auth_Service.dto.RefreshTokenRequestDTO;
import ke.co.legalbridge.Auth_Service.dto.RefreshTokenResponseDTO;
import ke.co.legalbridge.Auth_Service.dto.UserSessionDTO;
import ke.co.legalbridge.Auth_Service.service.SessionService;
import ke.co.legalbridge.sharedlibraries.response.ApiResponse;
import ke.co.legalbridge.sharedlibraries.response.ResponseEntityBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class SessionController {

    private final SessionService sessionService;

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponseDTO>> refreshToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO, HttpServletRequest request) {
        log.info("============= Called Refresh Token Request ================");

        RefreshTokenResponseDTO response = sessionService.refreshAccessToken(refreshTokenRequestDTO.getRefreshToken(), request);

        return ResponseEntityBuilder.ok(response, "Token refreshed successfully").build();

    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(@RequestBody LogoutRequestDTO requestDTO) {
        log.info("============= Calling logout Service ================");

        sessionService.logout(requestDTO.getRefreshToken());

        return ResponseEntityBuilder.ok(null, "Logged out successfully").build();
    }

    @PostMapping("/logout-all")
    @PreAuthorize("isAuthenticated")
    public ResponseEntity<ApiResponse<Object>> logoutAllSessions() {

        sessionService.logoutAllSessions();

        return ResponseEntityBuilder.ok(null, "All sessions logged out").build();
    }

    @GetMapping("/sessions")
    @PreAuthorize("isAuthenticated")
    public ResponseEntity<ApiResponse<List<UserSessionDTO>>> getActiveSessions() {

        List<UserSessionDTO> activeSessions = sessionService.getActiveSessions();

        return ResponseEntityBuilder.ok(activeSessions).build();
    }

}
