package ke.co.legalbridge.authservice.exception;

import ke.co.legalbridge.authservice.enumerations.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
public class BaseException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus status;
    private final LocalDateTime timestamp;
    private final Map<String, Object> details;
    private final String serviceName;

    protected BaseException(String message, ErrorCode errorCode, HttpStatus status, String serviceName) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
        this.serviceName = serviceName;
        this.timestamp = LocalDateTime.now();
        this.details = new HashMap<>();
    }

    protected BaseException(ErrorCode errorCode, String serviceName) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.status = errorCode.getStatus();
        this.serviceName = serviceName;
        this.timestamp = LocalDateTime.now();
        this.details = new HashMap<>();
    }

    protected BaseException(ErrorCode errorCode, String customMessage, String serviceName) {
        super(customMessage);
        this.errorCode = errorCode;
        this.status = errorCode.getStatus();
        this.serviceName = serviceName;
        this.timestamp = LocalDateTime.now();
        this.details = new HashMap<>();
    }

    public BaseException addDetail(String key, Object value) {
        this.details.put(key, value);
        return this;
    }

    public BaseException addDetails(Map<String, Object> details) {
        this.details.putAll(details);
        return this;
    }

}
