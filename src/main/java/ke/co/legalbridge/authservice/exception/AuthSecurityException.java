package ke.co.legalbridge.authservice.exception;


import ke.co.legalbridge.authservice.enumerations.ErrorCode;

public class AuthSecurityException extends BaseException {
    public AuthSecurityException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public AuthSecurityException(ErrorCode errorCode, String customMessage, String serviceName) {
        super(errorCode, customMessage, serviceName);
    }

    // ========== Authentication Factory Methods ==========
    public static AuthSecurityException invalidCredentials(String serviceName) {
        return new AuthSecurityException(ErrorCode.INVALID_CREDENTIALS, serviceName);
    }

    public static AuthSecurityException tokenExpired(String serviceName) {
        return new AuthSecurityException(ErrorCode.TOKEN_EXPIRED, serviceName);
    }

    public static AuthSecurityException invalidToken(String serviceName) {
        return new AuthSecurityException(ErrorCode.INVALID_TOKEN, serviceName);
    }

    public static AuthSecurityException accountLocked(String serviceName) {
        return new AuthSecurityException(ErrorCode.ACCOUNT_LOCKED, serviceName);
    }

    public static AuthSecurityException accountNotVerified(String serviceName) {
        return new AuthSecurityException(ErrorCode.ACCOUNT_NOT_VERIFIED, serviceName);
    }

    public static AuthSecurityException sessionExpired(String serviceName) {
        return new AuthSecurityException(ErrorCode.SESSION_EXPIRED, serviceName);
    }

    public static AuthSecurityException insufficientPermissions(String serviceName) {
        return new AuthSecurityException(ErrorCode.INSUFFICIENT_PERMISSIONS, serviceName);
    }

    public static AuthSecurityException forbidden(String serviceName) {
        return new AuthSecurityException(ErrorCode.FORBIDDEN_RESOURCE, serviceName);
    }

    public static AuthSecurityException unauthorized(String serviceName) {
        return new AuthSecurityException(ErrorCode.UNAUTHORIZED_ACCESS, serviceName);
    }

    @Override
    public AuthSecurityException addDetail(String key, Object value) {
        super.addDetail(key, value);
        return this;
    }

}
