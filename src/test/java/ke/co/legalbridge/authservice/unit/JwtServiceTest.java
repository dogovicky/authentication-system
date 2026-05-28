package ke.co.legalbridge.authservice.unit;

import ke.co.legalbridge.authservice.model.Role;
import ke.co.legalbridge.authservice.model.User;
import ke.co.legalbridge.authservice.security.JwtService;
import ke.co.legalbridge.authservice.utilities.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    private JwtService jwtService;
    private JwtUtil jwtUtil;

    private User testUser;


    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        String secret = Base64.getEncoder().encodeToString("VzCdtgrJC2xtyhckAoNgiMYvMEdxqkFITXho+yoIM5jNhB78Gnx8xIgreNkeoefmmT2EpFbQQFoYnhpvyH06/A==".getBytes());

        ReflectionTestUtils.setField(jwtService, "secretKey", secret);
        ReflectionTestUtils.setField(jwtService, "issuer", "auth-service");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 3600000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 604800000L);

        jwtUtil = new JwtUtil(jwtService);

        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("vicky@test.com")
                .roles(Set.of(
                        Role.builder()
                                .name("ROLE_USER")
                                .build()
                ))
                .build();
    }

    @Test
    void shouldGenerateValidAccessToken() {
        String token = jwtService.generateAccessToken(testUser);
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldExtractEmailFromToken() {
        String token = jwtService.generateAccessToken(testUser);
        String email = jwtUtil.extractEmail(token);
        assertEquals("vicky@test.com", email);
    }

    @Test
    void shouldValidateToken() {
        String token = jwtService.generateAccessToken(testUser);
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void shouldDetectExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", -1000L);
        String token = jwtService.generateAccessToken(testUser);
        assertFalse(jwtUtil.validateToken(token));
    }

    @Test
    void shouldExtractUserId() {
        String token = jwtService.generateAccessToken(testUser);
        String userId = jwtUtil.extractUserId(token);
        assertEquals(testUser.getId().toString(), userId);
    }

}
