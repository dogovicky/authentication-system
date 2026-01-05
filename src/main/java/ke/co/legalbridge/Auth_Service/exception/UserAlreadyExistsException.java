package ke.co.legalbridge.Auth_Service.exception;

import ke.co.legalbridge.sharedlibraries.enums.ErrorCode;
import ke.co.legalbridge.sharedlibraries.exceptions.BusinessException;

public class UserAlreadyExistsException extends BusinessException {

    private static final String SERVICE_NAME = "auth-service";

    public UserAlreadyExistsException(String email) {
        super(
                ErrorCode.USER_ALREADY_EXISTS,
                String.format("User with email %s already exists", email),
                SERVICE_NAME
        );

        this.addDetail("email", email);
        this.addDetail("field", "email");
    }

    public UserAlreadyExistsException(String email, String message) {
        super(ErrorCode.USER_ALREADY_EXISTS, message, SERVICE_NAME);
        this.addDetail("email", email);
    }
}
