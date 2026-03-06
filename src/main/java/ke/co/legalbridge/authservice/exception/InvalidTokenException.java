package ke.co.legalbridge.authservice.exception;

import ke.co.legalbridge.sharedlibraries.enums.ErrorCode;
import ke.co.legalbridge.sharedlibraries.exceptions.AuthSecurityException;

public class InvalidTokenException extends AuthSecurityException {
    private static final String SERVICE_NAME = "auth-service";

    public InvalidTokenException(String message) {
        super(ErrorCode.INVALID_TOKEN, message, SERVICE_NAME);
    }
}
