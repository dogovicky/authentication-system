package ke.co.legalbridge.authservice.enumerations;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // ==================== AUTHENTICATION ERRORS (1000-1099) ====================
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_1001", "Invalid email or password"),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "AUTH_1002", "Account has been locked due to suspicious activity"),
    ACCOUNT_NOT_VERIFIED(HttpStatus.FORBIDDEN, "AUTH_1003", "Account email not verified"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_1004", "Invalid or expired authentication token"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_1005", "Authentication token has expired"),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_1006", "Invalid refresh token"),
    SESSION_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_1007", "Session has expired"),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "AUTH_1008", "Unauthorized access to resource"),
    PASSWORD_RESET_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "AUTH_1009", "Password reset token is invalid or expired"),
    ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, "AUTH_1010", "Account has been disabled"),

    // ==================== AUTHORIZATION ERRORS (1100-1199) ====================
    INSUFFICIENT_PERMISSIONS(HttpStatus.FORBIDDEN, "AUTHZ_1101", "Insufficient permissions to perform this action"),
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTHZ_1102", "Specified role not found"),
    FORBIDDEN_RESOURCE(HttpStatus.FORBIDDEN, "AUTHZ_1103", "Access to this resource is forbidden"),
    INVALID_USER_ROLE(HttpStatus.FORBIDDEN, "AUTHZ_1104", "User type not authorized for this operation"),

    // ==================== USER/PROFILE ERRORS (1200-1299) ====================
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_1201", "User not found"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_1202", "User with this email already exists"),

    // ==================== EXTERNAL SERVICE ERRORS (2400-2499) ====================
    EXTERNAL_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "EXT_2401", "External service is currently unavailable"),
    EXTERNAL_SERVICE_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "EXT_2402", "External service request timed out"),
    EXTERNAL_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "EXT_2403", "External service returned an error"),

    // ==================== VALIDATION ERRORS (2000-2099) ====================
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALID_2001", "Validation error occurred"),
    REQUIRED_FIELD_MISSING(HttpStatus.BAD_REQUEST, "VALID_2002", "Required field is missing"),
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "VALID_2003", "Invalid email format"),
    INVALID_PHONE_FORMAT(HttpStatus.BAD_REQUEST, "VALID_2004", "Invalid phone number format"),
    PASSWORD_TOO_WEAK(HttpStatus.BAD_REQUEST, "VALID_2005", "Password does not meet security requirements"),
    INVALID_DATE_FORMAT(HttpStatus.BAD_REQUEST, "VALID_2006", "Invalid date format"),
    INVALID_URL_FORMAT(HttpStatus.BAD_REQUEST, "VALID_2007", "Invalid URL format"),
    FIELD_VALUE_TOO_LONG(HttpStatus.BAD_REQUEST, "VALID_2008", "Field value exceeds maximum length"),
    FIELD_VALUE_TOO_SHORT(HttpStatus.BAD_REQUEST, "VALID_2009", "Field value is below minimum length"),
    INVALID_ENUM_VALUE(HttpStatus.BAD_REQUEST, "VALID_2010", "Invalid enum value provided"),

    // ==================== GENERAL/SYSTEM ERRORS (9000-9099) ====================
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_9001", "An internal server error occurred"),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "SYS_9002", "Service temporarily unavailable"),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_9003", "Database operation failed"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "SYS_9004", "Requested resource not found"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "SYS_9005", "Invalid request"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "SYS_9006", "HTTP method not allowed"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "SYS_9007", "Unsupported media type"),
    REQUEST_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "SYS_9008", "Request timeout"),
    CONFLICT(HttpStatus.CONFLICT, "SYS_9009", "Resource conflict occurred"),
    GONE(HttpStatus.GONE, "SYS_9010", "Resource no longer available"),
    PRECONDITION_FAILED(HttpStatus.PRECONDITION_FAILED, "SYS_9011", "Precondition failed");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    /*
     * Get HTTP status as Integer
     */
    public int getStatusCode() {
        return status.value();
    }

    /*
     * Find ErrorCode by code string
     */
    public static ErrorCode fromCode(String code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.code.equals(code)) {
                return errorCode;
            }
        }
        return INTERNAL_SERVER_ERROR;
    }

}

