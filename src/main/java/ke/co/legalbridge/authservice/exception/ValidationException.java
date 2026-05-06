package ke.co.legalbridge.authservice.exception;

import ke.co.legalbridge.authservice.enumerations.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class ValidationException extends BaseException {
    public ValidationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ValidationException(ErrorCode errorCode, String customMessage, String serviceName) {
        super(errorCode, customMessage, serviceName);
    }

    public ValidationException(String message, String serviceName) {
        super(message, ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, serviceName);
    }

    public ValidationException(String message, Map<String, String> fieldErrors, String serviceName) {
        super(message, ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, serviceName);
        addDetail("fieldErrors", fieldErrors);
    }

    public ValidationException addFieldError(String field, String error) {
        Map<String, String> fieldErrors = (Map<String, String>) getDetails().get("fieldErrors");
        if (fieldErrors == null) {
            fieldErrors = new HashMap<>();
            addDetail("fieldErrors", fieldErrors);
        }
        fieldErrors.put(field, error);
        return this;
    }

     // ========== Factory Methods ==========
    public static ValidationException requiredField(String fieldName, String serviceName) {
        return new ValidationException(ErrorCode.REQUIRED_FIELD_MISSING, serviceName)
                .addFieldError(fieldName, "Field is required");
    }

    public static ValidationException invalidEmail(String serviceName) {
        return new ValidationException(ErrorCode.INVALID_EMAIL_FORMAT, serviceName);
    }

    public static ValidationException invalidPhone(String serviceName) {
        return new ValidationException(ErrorCode.INVALID_PHONE_FORMAT, serviceName);
    }

    public static ValidationException passwordTooWeak(String serviceName) {
        return new ValidationException(ErrorCode.PASSWORD_TOO_WEAK, serviceName);
    }

    public static ValidationException invalidFormat(String fieldName, String serviceName) {
        return new ValidationException(ErrorCode.VALIDATION_ERROR, serviceName)
                .addFieldError(fieldName, "Invalid format");
    }

    @Override
    public ValidationException addDetail(String key, Object value) {
        super.addDetail(key, value);
        return this;
    }

}
