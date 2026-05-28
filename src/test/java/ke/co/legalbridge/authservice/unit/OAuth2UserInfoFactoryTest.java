package ke.co.legalbridge.authservice.unit;

import ke.co.legalbridge.authservice.enumerations.OAuth2Provider;
import ke.co.legalbridge.authservice.oauth2.OAuth2UserInfo;
import ke.co.legalbridge.authservice.oauth2.OAuth2UserInfoFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class OAuth2UserInfoFactoryTest {

    @Test
    void shouldCreateGoogleUserInfor() {
        Map<String, Object> attributes = Map.of(
                "sub", "google-id-123",
                "email", "vicky@gmail.com",
                "name", "Vicky",
                "picture", "https://avatar.url"
        );

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.create("google", attributes);

        assertEquals("google-id-123", userInfo.getId());
        assertEquals("vicky@gmail.com", userInfo.getEmail());
        assertEquals(OAuth2Provider.GOOGLE, userInfo.getProvider());
    }

    @Test
    void shouldCreateGitHubUserInfo() {
        Map<String, Object> attributes = Map.of(
                "id", 149660188,
                "login", "dogovicky",
                "name", "Vicky",
                "avatar_url", "http://avatar.url"
        );

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.create("github", attributes);

        assertEquals("149660188", userInfo.getId()); // String.valueOf
        assertEquals(OAuth2Provider.GITHUB, userInfo.getProvider());
    }

    @Test
    void shouldFallbackToLoginWhenNameIsNull() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", 149660188);
        attributes.put("login", "dogovicky");
        attributes.put("name", null);

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.create("github", attributes);

        assertEquals("dogovicky", userInfo.getName());
    }

    @Test
    void shouldThrowForUnknownProvider() {
        assertThrows(IllegalArgumentException.class,
                () -> OAuth2UserInfoFactory.create("twitter", Map.of()));
    }

}
