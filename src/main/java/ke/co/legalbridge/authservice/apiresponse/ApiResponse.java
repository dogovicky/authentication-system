package ke.co.legalbridge.authservice.apiresponse;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import ke.co.legalbridge.authservice.enumerations.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    private String errorCode;
    private List<String> errors;
    private Map<String, String> fieldErrors;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private String requestId;
    private String serviceName;
    private String path;

    private PaginationInfo pagination;
    private long processingTimeMs;
    private Map<String, String> links;
    private String details;

    // =========== SUCCESS BUILDERS ==============

    public static <T> ApiResponseBuilder<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .timestamp(now());
    }

    public static <T> ApiResponseBuilder<T> success(T data) {
        return success(data, null);
    }

    public static <T> ApiResponseBuilder<T> success() {
        return success(null, null);
    }


    // ========== ERROR BUILDERS ===========

    public static <T> ApiResponseBuilder<T> error(int status, String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now());
    }

    public static <T> ApiResponseBuilder<T> badRequest(String message) {
        return error(400, message, "BAD_REQUEST");
    }

    public static <T> ApiResponseBuilder<T> validationError(String message) {
        return error(422, message, "VALIDATION_ERROR");
    }

    public static <T> ApiResponseBuilder<T> internalServerError(String message) {
        return error(500, message, "INTERNAL_SERVER_ERROR");
    }

    // =========== CUSTOM BUILDER METHODS =============

    public static class ApiResponseBuilder<T> {

        public ApiResponseBuilder<T> withProcessingTime(long startTimeMs) {
            this.processingTimeMs = System.currentTimeMillis() - startTimeMs;
            return this;
        }

        public ApiResponseBuilder<T> withValidationErrors(Map<String, String> fieldErrors) {
            if (this.fieldErrors == null) this.fieldErrors = new HashMap<>();
            this.fieldErrors.putAll(fieldErrors);
            return this;
        }

        public ApiResponseBuilder<T> addFieldError(String field, String error) {
            if (this.fieldErrors == null) this.fieldErrors = new HashMap<>();
            this.fieldErrors.put(field, error);
            return this;
        }

        public ApiResponseBuilder<T> addError(String error) {
            if (this.errors == null) this.errors = new ArrayList<>();
            this.errors.add(error);
            return this;
        }

        public ApiResponseBuilder<T> withErrorDetail(String message, String details) {
            this.message = message;
            this.details = details;
            return this;
        }

        public ApiResponseBuilder<T> withMetadata(String requestId, String serviceName, String path) {
            this.requestId = requestId;
            this.serviceName = serviceName;
            this.path = path;
            return this;
        }

        public ApiResponseBuilder<T> withLink(String rel, String href) {
            if (this.links == null) this.links = new HashMap<>();
            this.links.put(rel, href);
            return this;
        }
    }

    // ================= Helper Methods ================
    private static LocalDateTime now() {
        Clock clock = Clock.systemUTC();
        return LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
    }

}
