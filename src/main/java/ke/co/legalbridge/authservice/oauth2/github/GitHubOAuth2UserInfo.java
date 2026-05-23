package ke.co.legalbridge.authservice.oauth2.github;

import ke.co.legalbridge.authservice.enumerations.OAuth2Provider;
import ke.co.legalbridge.authservice.oauth2.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class GitHubOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;


    @Override
    public String getId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        Object name = attributes.get("name");
        return name != null ? (String) name : (String) attributes.get("login"); // Fallback to username
    }

    @Override
    public String getAvatarUrl() {
        return (String) attributes.get("avatar_url");
    }

    @Override
    public OAuth2Provider getProvider() {
        return OAuth2Provider.GITHUB;
    }
}
