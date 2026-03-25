package ke.co.legalbridge.authservice.exception.handlers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import ke.co.legalbridge.authservice.apiresponse.ApiResponse;
import ke.co.legalbridge.authservice.apiresponse.ResponseEntityBuilder;
import ke.co.legalbridge.authservice.enumerations.ErrorCode;
import ke.co.legalbridge.authservice.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.messaging.handler.annotation.support.MethodArgumentTypeMismatchException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    @Value("${spring.application.name:unknown-service}")
    private String serviceName;

    @Value("${app.show-stack-trace:false}")
    private boolean showStackTrace;

    // ======================= CUSTOM EXCEPTION HANDLERS =========================
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        if (ex.getStatus().is5xxServerError()) {
            log.error("Business exception: {} - {}", ex.getErrorCode(), ex.getMessage(), ex);
        } else {
            log.warn("Business exception: {} - {}", ex.getErrorCode(), ex.getMessage());
        }
        return buildErrorResponse(ex, request);
    }

    @ExceptionHandler(AuthSecurityException.class)
    public ResponseEntity<ApiResponse<Void>> handleSecurityException(AuthSecurityException ex, HttpServletRequest request) {
        log.warn("Security exception: {} - {} from IP: {}", ex.getErrorCode(), ex.getMessage(), getClientIp(request));
        return buildErrorResponse(ex, request);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(ValidationException ex, HttpServletRequest request) {
        log.warn("Validation exception: {}", ex.getMessage());

        @SuppressWarnings("unchecked")
        Map<String, String> fieldErrors = (Map<String, String>) ex.getDetails().get("fieldErrors");

        ResponseEntityBuilder<Void> builder = ResponseEntityBuilder
                .<Void>validationError(ex.getMessage())
                .withMetadata(getRequestId(request), resolveServiceName(ex), request.getRequestURI());

        if (fieldErrors != null && !fieldErrors.isEmpty()) {
            builder.withFieldErrors(fieldErrors);
        }

        return builder.build();
    }

    @ExceptionHandler(TechnicalException.class)
    public ResponseEntity<ApiResponse<Void>> handleTechnicalException(TechnicalException ex, HttpServletRequest request) {
        log.error("Technical exception: {} - {}", ex.getErrorCode(), ex.getMessage(), ex);

        ResponseEntityBuilder<Void> builder = ResponseEntityBuilder
                .<Void>internalServerError(ex.getMessage())
                .withMetadata(getRequestId(request), resolveServiceName(ex), request.getRequestURI());

        if (showStackTrace && ex.getCause() != null) {
            builder.withErrorDetail("cause", ex.getCause().getMessage())
                    .withErrorDetail("causeType", ex.getCause().getClass().getSimpleName());
        }

        return builder.build();
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleServiceException(BaseException ex, HttpServletRequest request) {
        log.error("Service exception: {} - {}", ex.getErrorCode(), ex.getMessage(), ex);
        return buildErrorResponse(ex, request);
    }

    // ==================== VALIDATION EXCEPTIONS ====================

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResponse<Void>> handleBindingValidation(BindException ex, HttpServletRequest request) {
        log.warn("Validation failed: {} errors", ex.getBindingResult().getErrorCount());

        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : "Invalid value",
                        (a, b) -> a
                ));

        return ResponseEntityBuilder.<Void>validationError("Validation failed")
                .withFieldErrors(fieldErrors)
                .withMetadata(getRequestId(request), serviceName, request.getRequestURI())
                .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        log.warn("Constraint violation: {}", ex.getMessage());

        Map<String, String> fieldErrors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (a, b) -> a
                ));

        return ResponseEntityBuilder.<Void>validationError("Constraint validation failed")
                .withFieldErrors(fieldErrors)
                .withMetadata(getRequestId(request), serviceName, request.getRequestURI())
                .build();
    }

    // ==================== SPRING SECURITY EXCEPTIONS ====================

    @ExceptionHandler({BadCredentialsException.class, InsufficientAuthenticationException.class})
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(Exception ex, HttpServletRequest request) {
        log.warn("Authentication failed from IP: {} - {}", getClientIp(request), ex.getMessage());

        return ResponseEntityBuilder.<Void>error(ErrorCode.INVALID_CREDENTIALS)
                .withMetadata(getRequestId(request), serviceName, request.getRequestURI())
                .withErrorDetail("clientIp", getClientIp(request))
                .build();
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied from IP: {} to path: {}", getClientIp(request), request.getRequestURI());

        return ResponseEntityBuilder.<Void>error(ErrorCode.INSUFFICIENT_PERMISSIONS)
                .withMetadata(getRequestId(request), serviceName, request.getRequestURI())
                .withErrorDetail("requiredPath", request.getRequestURI())
                .withErrorDetail("method", request.getMethod())
                .build();
    }

    // ==================== HTTP EXCEPTIONS ====================

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParams(MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.warn("Missing required parameter: {} (type: {})", ex.getParameterName(), ex.getParameterType());

        return ResponseEntityBuilder.<Void>error(ErrorCode.REQUIRED_FIELD_MISSING)
                .withMetadata(getRequestId(request), serviceName, request.getRequestURI())
                .build();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String expectedType = ex.getClass().getComponentType() != null ? ex.getClass().getComponentType().getSimpleName() : "unknown";
        log.warn("Type mismatch for parameter '{}': expected {}, got {}", ex.getClass().getName(), expectedType, ex.getCause().getMessage());

        return ResponseEntityBuilder.<Void>badRequest(
                        String.format("Invalid value for parameter '%s'. Expected type: %s", ex.getClass().getName(), expectedType))
                .withErrorDetail("providedValue", ex.getMessage())
                .withErrorDetail("expectedType", expectedType)
                .withMetadata(getRequestId(request), serviceName, request.getRequestURI())
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed JSON request: {}", ex.getMessage());

        return ResponseEntityBuilder.<Void>badRequest("Invalid request body")
                .withErrorDetail("cause", ex.getCause() != null ? ex.getCause().getMessage() : "Malformed JSON")
                .withErrorDetail("hint", "Please ensure the request body is valid JSON")
                .withMetadata(getRequestId(request), serviceName, request.getRequestURI())
                .build();
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Method not allowed: {} for path: {}", ex.getMethod(), request.getRequestURI());

        String supportedMethods = ex.getSupportedHttpMethods() != null
                ? ex.getSupportedHttpMethods().stream().map(Object::toString).collect(Collectors.joining(", "))
                : "NONE";

        return ResponseEntityBuilder.<Void>error(ErrorCode.METHOD_NOT_ALLOWED)
                .withErrorDetail("method", ex.getMethod())
                .withErrorDetail("supportedMethods", supportedMethods)
                .withMetadata(getRequestId(request), serviceName, request.getRequestURI())
                .build();
    }

    // ==================== CATCH-ALL ====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);

        ResponseEntityBuilder<Void> builder = ResponseEntityBuilder
                .<Void>internalServerError("An unexpected error occurred")
                .withMetadata(getRequestId(request), serviceName, request.getRequestURI());

        if (showStackTrace) {
            builder.withErrorDetail("exceptionType", ex.getClass().getSimpleName())
                    .withErrorDetail("exceptionMessage", ex.getMessage());
            if (ex.getCause() != null) {
                builder.withErrorDetail("cause", ex.getCause().getMessage())
                        .withErrorDetail("causeType", ex.getCause().getClass().getSimpleName());
            }
        }

        return builder.build();
    }

    // ==================== UTILITIES ====================

    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(BaseException ex, HttpServletRequest request) {
        return ResponseEntityBuilder.<Void>error(ex.getErrorCode())
                .withMetadata(getRequestId(request), resolveServiceName(ex), request.getRequestURI())
                .build();
    }

    private String resolveServiceName(BaseException ex) {
        return ex.getServiceName() != null ? ex.getServiceName() : serviceName;
    }

    private String getRequestId(HttpServletRequest request) {
        String id = request.getHeader("X-Request-ID");
        if (id == null || id.isBlank()) id = request.getHeader("X-Correlation-ID");
        if (id == null || id.isBlank()) id = UUID.randomUUID().toString();
        return id;
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) return realIp;
        return request.getRemoteAddr();
    }

}
