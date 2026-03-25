package ke.co.legalbridge.authservice.apiresponse;

import ke.co.legalbridge.authservice.enumerations.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ResponseEntityBuilder<T> {

    private final ApiResponse.ApiResponseBuilder<T> apiResponseBuilder;
    private final HttpStatus status;

    private ResponseEntityBuilder(ApiResponse.ApiResponseBuilder<T> builder, HttpStatus status) {
        this.apiResponseBuilder = builder;
        this.status = status;
    }

    // ==================== SUCCESS RESPONSES ====================

    public static <T> ResponseEntityBuilder<T> ok(T data, String message) {
        return new ResponseEntityBuilder<>(ApiResponse.success(data, message), HttpStatus.OK);
    }

    public static <T> ResponseEntityBuilder<T> ok(T data) {
        return ok(data, null);
    }

    public static <T> ResponseEntityBuilder<T> ok() {
        return ok(null, null);
    }

    public static <T> ResponseEntityBuilder<T> created(T data, String message) {
        return new ResponseEntityBuilder<>(ApiResponse.success(data, message), HttpStatus.CREATED);
    }

    public static <T> ResponseEntityBuilder<T> created(T data) {
        return created(data, "Resource created successfully");
    }

    public static <T> ResponseEntity<ApiResponse<T>> noContent() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).<ApiResponse<T>>build();
    }

    // ==================== ERROR RESPONSES ====================

    public static <T> ResponseEntityBuilder<T> error(ErrorCode errorCode) {
        return new ResponseEntityBuilder<>(
                ApiResponse.<T>error(errorCode.getStatusCode(), errorCode.getMessage(), errorCode.getCode()),
                errorCode.getStatus()
        );
    }

    public static <T> ResponseEntityBuilder<T> badRequest(String message) {
        return new ResponseEntityBuilder<>(ApiResponse.badRequest(message), HttpStatus.BAD_REQUEST);
    }

    public static <T> ResponseEntityBuilder<T> validationError(String message) {
        return new ResponseEntityBuilder<>(ApiResponse.validationError(message), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public static <T> ResponseEntityBuilder<T> validationError(BindingResult bindingResult) {
        Map<String, String> fieldErrors = bindingResult.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : "Invalid value",
                        (a, b) -> a
                ));
        return new ResponseEntityBuilder<>(
                ApiResponse.<T>validationError("Validation failed").withValidationErrors(fieldErrors),
                HttpStatus.UNPROCESSABLE_ENTITY
        );
    }

    public static <T> ResponseEntityBuilder<T> internalServerError(String message) {
        return new ResponseEntityBuilder<>(ApiResponse.internalServerError(message), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static <T> ResponseEntityBuilder<T> serviceUnavailable(String message) {
        return new ResponseEntityBuilder<>(
                ApiResponse.<T>error(503, message, "SERVICE_UNAVAILABLE"),
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    // ==================== BUILDER METHODS ====================

    public ResponseEntityBuilder<T> withMetadata(String requestId, String serviceName, String path) {
        apiResponseBuilder.withMetadata(requestId, serviceName, path);
        return this;
    }

    public ResponseEntityBuilder<T> withProcessingTime(long startTimeMs) {
        apiResponseBuilder.withProcessingTime(startTimeMs);
        return this;
    }

    public ResponseEntityBuilder<T> withFieldErrors(Map<String, String> fieldErrors) {
        apiResponseBuilder.withValidationErrors(fieldErrors);
        return this;
    }

    public ResponseEntityBuilder<T> withLink(String rel, String href) {
        apiResponseBuilder.withLink(rel, href);
        return this;
    }

    public ResponseEntityBuilder<T> withErrorDetail(String message, String details) {
        apiResponseBuilder.withErrorDetail(message, details);
        return this;
    }

    // ==================== BUILD ====================

    public ResponseEntity<ApiResponse<T>> build() {
        return ResponseEntity.status(status).body(apiResponseBuilder.build());
    }

    // ==================== UTILITIES ====================

    public static Map<String, Object> errorDetailsFrom(Exception ex) {
        Map<String, Object> details = new HashMap<>();
        details.put("exceptionType", ex.getClass().getSimpleName());
        if (ex.getMessage() != null) details.put("exceptionMessage", ex.getMessage());
        return details;
    }

}
