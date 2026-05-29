package ke.co.legalbridge.authservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import ke.co.legalbridge.authservice.configuration.TestContainersConfig;
import ke.co.legalbridge.authservice.dto.login.LoginRequestDTO;
import ke.co.legalbridge.authservice.dto.registration.SignUpRequestDTO;
import ke.co.legalbridge.authservice.model.Privilege;
import ke.co.legalbridge.authservice.model.Role;
import ke.co.legalbridge.authservice.model.User;
import ke.co.legalbridge.authservice.repository.PrivilegeRepository;
import ke.co.legalbridge.authservice.repository.RoleRepository;
import ke.co.legalbridge.authservice.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class AuthControllerIT extends TestContainersConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PrivilegeRepository privilegeRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        SignUpRequestDTO requestDTO = SignUpRequestDTO.builder()
                .email("newuser@test.com")
                .password("Strongpass@123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("newuser@test.com"))
                .andExpect(jsonPath("$.data.verified").value(false));
    }

    @Test
    void shouldRejectDuplicateEmail() throws Exception{
        // Create User first
        User existing = User.builder()
                .email("existing@email.com")
                .passwordHash(passwordEncoder.encode("password"))
                .isVerified(true)
                .isActive(true)
                .build();

        userRepo.save(existing);

        SignUpRequestDTO requestDTO = SignUpRequestDTO.builder()
                .email(existing.getEmail())
                .password("StrongPassword@123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldLoginSuccessfully() throws Exception{

        Privilege privilege = Privilege.builder()
                .name("WRITE_ACCESS")
                .build();

        Role role = Role.builder()
                .name("USER")
                .privileges(Set.of(privilege))
                .build();

        privilegeRepo.save(privilege);
        roleRepository.save(role);

        User user = User.builder()
                .email("login@test.com")
                .passwordHash(passwordEncoder.encode("StrongPass@123"))
                .isVerified(true)
                .isActive(true)
                .roles(Set.of(role))
                .build();
        userRepo.save(user);

        LoginRequestDTO requestDTO = LoginRequestDTO.builder()
                .email("login@test.com")
                .password("StrongPass@123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("New device detected. Enter the OTP sent to your email."))
                .andReturn();

        System.out.println(result.getResponse().getContentAsString());
    }

    @Test
    void shouldRejectInvalidCredentials() throws Exception {
        LoginRequestDTO requestDTO = LoginRequestDTO.builder()
                .email("nonexistent@test.com")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized());
    }

}
