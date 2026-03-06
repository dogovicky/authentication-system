package ke.co.legalbridge.authservice.exception;

import ke.co.legalbridge.sharedlibraries.enums.ErrorCode;
import ke.co.legalbridge.sharedlibraries.exceptions.ValidationException;

public class InvalidRegistrationException extends ValidationException {

    private static final String SERVICE_NAME = "auth-service";

    public InvalidRegistrationException(String message) {
        super(ErrorCode.INTERNAL_SERVER_ERROR, message, SERVICE_NAME);
    }

    public static InvalidRegistrationException missingUserType() {
        InvalidRegistrationException ex = new InvalidRegistrationException("User type is required");
        ex.addDetail("userType", "User type must be specified");
        return ex;
    }

    public static InvalidRegistrationException invalidUserType(String userType) {
        InvalidRegistrationException ex = new InvalidRegistrationException(
                String.format("Invalid user type: %s", userType)
        );
        ex.addDetail("userType", "Must be CLIENT, LAWYER, LAW_FIRM, or ADMIN");
        ex.addDetail("providedValue", userType);
        return ex;
    }

    public static InvalidRegistrationException invalidEmail(String email) {
        InvalidRegistrationException ex = new InvalidRegistrationException("Invalid email format");
        ex.addDetail("email", "Please provide a valid email address");
        ex.addDetail("providedEmail", email);
        return ex;
    }
}
