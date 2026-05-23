package ke.co.legalbridge.authservice.services;

import ke.co.legalbridge.authservice.enumerations.OAuth2Provider;
import ke.co.legalbridge.authservice.model.User;
import ke.co.legalbridge.authservice.oauth2.OAuth2UserInfo;
import ke.co.legalbridge.authservice.oauth2.OAuth2UserInfoFactory;
import ke.co.legalbridge.authservice.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepo userRepo;
    private final GitHubEmailFetcher emailFetcher;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2User oAuth2User = super.loadUser(userRequest);

            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            OAuth2UserInfo userInfo = OAuth2UserInfoFactory.create(registrationId, oAuth2User.getAttributes());

            log.info("============ OAuth2 user loaded from provider: {} =================", registrationId);

            // Resolve email — fetch from GitHub API if null
            String resolvedEmail = userInfo.getEmail();

            if (resolvedEmail == null && OAuth2Provider.of(registrationId) == OAuth2Provider.GITHUB) {
                String token = userRequest.getAccessToken().getTokenValue();
                resolvedEmail = emailFetcher.fetchPrimaryEmail(token);
                log.info("================ GitHub email fetched: {} =================", resolvedEmail);
            }

            // Validate after fetch
            if (resolvedEmail == null || resolvedEmail.isBlank()) {
                log.error("!!!!!!!!!!!!!!! Failed to resolve email from provider !!!!!!!!!!!!!!!!!!");
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("email_not_found"), "Email not returned by provider"
                );
            }

            log.info("UserInfo — id: {}, email: {}, name: {}", userInfo.getId(), resolvedEmail, userInfo.getName());

            final String email = resolvedEmail;

            User user = userRepo.findByEmailWithRoles(email)
                    .map(existing -> updateExistingUser(existing, userInfo))
                    .orElseGet(() -> registerNewUser(userInfo, email));

            log.info("=============== User resolved: {} | provider: {} ==================", user.getEmail(), user.getProvider());

            Map<String, Object> modifiedAttributes = new HashMap<>(oAuth2User.getAttributes());
            modifiedAttributes.put("email", email);

            return new DefaultOAuth2User(
                    List.of(new SimpleGrantedAuthority("ROLE_USER")),
                    modifiedAttributes,
                    getNameAttributeKey(registrationId)
            );

        } catch (OAuth2AuthenticationException ex) {
            log.error("!!!!!!!!!!! OAuth2 error: {} !!!!!!!!!!!!!", ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("!!!!!!!!!! Unexpected error loading OAuth2 user: {} !!!!!!!!!!!!!", ex.getMessage(), ex);
            throw new OAuth2AuthenticationException(new OAuth2Error("user_load_error"), ex.getMessage(), ex);
        }
    }

    private User registerNewUser(OAuth2UserInfo userInfo, String email) {
        User user = User.builder()
                .email(email)
                .provider(userInfo.getProvider())
                .providerId(userInfo.getId())
                .isVerified(true) // OAuth2 Users are pre-verified by providers
                .isActive(true)
                .build();

        log.info("=============== Registering new User via OAuth2: {}, from: {} ===================", userInfo.getEmail(), userInfo.getProvider());
        return userRepo.save(user);
    }

    private User updateExistingUser(User user, OAuth2UserInfo userInfo) {
        user.setProvider(userInfo.getProvider());
        return userRepo.save(user);
    }

    private String getNameAttributeKey(String registrationId) {
        return switch(OAuth2Provider.of(registrationId)) {
            case GITHUB -> "id";
            default -> "sub";
        };
    }
}
