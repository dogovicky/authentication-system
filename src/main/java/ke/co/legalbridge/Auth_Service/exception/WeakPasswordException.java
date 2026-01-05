package ke.co.legalbridge.Auth_Service.exception;

import ke.co.legalbridge.sharedlibraries.exceptions.ValidationException;
import ke.co.legalbridge.sharedlibraries.enums.ErrorCode;
import ke.co.legalbridge.sharedlibraries.security.PasswordUtil;

import java.util.List;

public class WeakPasswordException extends ValidationException {

    private static final String SERVICE_NAME = "auth-service";

    public WeakPasswordException(String reason) {
        super(ErrorCode.PASSWORD_TOO_WEAK, reason, SERVICE_NAME);
        this.addDetail("password", reason);
    }

    public WeakPasswordException(List<String> errors) {
        super(ErrorCode.PASSWORD_TOO_WEAK, "Password does not meet security requirements", SERVICE_NAME);
        errors.forEach(error -> this.addDetail("password", error));
        this.addDetail("passwordErrors", errors);
    }

    /**
     * Create exception from PasswordUtil validation result
     */
    public static WeakPasswordException fromValidationResult(PasswordUtil.PasswordValidationResult result) {
        return new WeakPasswordException(result.getErrors());
    }
}