package ke.co.legalbridge.authservice.oauth2;

import ke.co.legalbridge.authservice.enumerations.OAuth2Provider;
import ke.co.legalbridge.authservice.oauth2.github.GitHubOAuth2UserInfo;
import ke.co.legalbridge.authservice.oauth2.google.GoogleOAuth2UserInfo;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo create(String registrationId, Map<String, Object> attributes) {
        return switch(OAuth2Provider.of(registrationId)) {
            case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
            case GITHUB -> new GitHubOAuth2UserInfo(attributes);
        };
    }

}
