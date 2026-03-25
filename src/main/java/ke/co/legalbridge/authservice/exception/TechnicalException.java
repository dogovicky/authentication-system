package ke.co.legalbridge.authservice.exception;

import ke.co.legalbridge.authservice.enumerations.ErrorCode;
import org.springframework.http.HttpStatus;

public class TechnicalException extends BaseException {
    public TechnicalException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public TechnicalException(ErrorCode errorCode, String customMessage, String serviceName) {
        super(errorCode, customMessage, serviceName);
    }

    public TechnicalException(String message, String serviceName) {
        super(message, "TECHNICAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, serviceName);
    }

    // ========== Database/System Factory Methods ==========
    public static TechnicalException databaseError(String serviceName) {
        return new TechnicalException(ErrorCode.DATABASE_ERROR, serviceName);
    }

    public static TechnicalException internalServerError(String serviceName) {
        return new TechnicalException(ErrorCode.INTERNAL_SERVER_ERROR, serviceName);
    }

    public static TechnicalException serviceUnavailable(String serviceName) {
        return new TechnicalException(ErrorCode.SERVICE_UNAVAILABLE, serviceName);
    }

    // ========== External Service Factory Methods ==========
    public static TechnicalException externalServiceUnavailable(String externalService, String serviceName) {
        return new TechnicalException(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE, serviceName)
                .addDetail("externalService", externalService);
    }

    public static TechnicalException externalServiceTimeout(String externalService, String serviceName) {
        return new TechnicalException(ErrorCode.EXTERNAL_SERVICE_TIMEOUT, serviceName)
                .addDetail("externalService", externalService);
    }

    public static TechnicalException externalServiceError(String externalService, String message, String serviceName) {
        return new TechnicalException(ErrorCode.EXTERNAL_SERVICE_ERROR, serviceName)
                .addDetail("externalService", externalService)
                .addDetail("errorMessage", message);
    }

    @Override
    public TechnicalException addDetail(String key, Object value) {
        super.addDetail(key, value);
        return this;
    }

}
