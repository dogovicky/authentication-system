package ke.co.legalbridge.authservice.exception;

import ke.co.legalbridge.authservice.enumerations.ErrorCode;

/**
 * For business logic violations, resource conflicts, and domain-specific rules
 * Use for: NOT_FOUND, ALREADY_EXISTS, INVALID_STATE, BUSINESS_RULE_VIOLATION
 */

public class BusinessException extends BaseException {
    public BusinessException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public BusinessException(ErrorCode errorCode, String customMessage, String serviceName) {
        super(errorCode, customMessage, serviceName);
    }

    // ========== Resource Not Found Factory Methods ==========
    public static BusinessException userNotFound(String userId, String serviceName) {
        return new BusinessException(ErrorCode.USER_NOT_FOUND, serviceName)
                .addDetail("userId", userId);
    }

    public static BusinessException resourceNotFound(String resourceType, String resourceId, String serviceName) {
        return new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, serviceName)
                .addDetail("resourceType", resourceType)
                .addDetail("resourceId", resourceId);
    }

    // ========== Conflict/Already Exists Factory Methods ==========
    public static BusinessException userAlreadyExists(String email, String serviceName) {
        return new BusinessException(ErrorCode.USER_ALREADY_EXISTS, serviceName)
                .addDetail("email", email);
    }

    @Override
    public BusinessException addDetail(String key, Object value) {
        super.addDetail(key, value);
        return this;
    }

}
