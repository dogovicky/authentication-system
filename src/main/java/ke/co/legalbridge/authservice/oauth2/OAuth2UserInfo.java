package ke.co.legalbridge.authservice.oauth2;

import ke.co.legalbridge.authservice.enumerations.OAuth2Provider;

public interface OAuth2UserInfo {
    String getId();
    String getEmail();
    String getName();
    String getAvatarUrl();
    OAuth2Provider getProvider();
}
