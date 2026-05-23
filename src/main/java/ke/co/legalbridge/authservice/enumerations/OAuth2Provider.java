package ke.co.legalbridge.authservice.enumerations;

public enum OAuth2Provider {

    GOOGLE, GITHUB;

    public static OAuth2Provider of(String registrationId) {
        return switch(registrationId.toLowerCase()) {
            case "google" -> GOOGLE;
            case "github" -> GITHUB;
            default -> throw new IllegalArgumentException("Unknown provider: " + registrationId);
        };
    }

}
