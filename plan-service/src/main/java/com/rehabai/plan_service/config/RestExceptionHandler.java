package com.rehabai.plan_service.config;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "bad_request";
        String low = msg.toLowerCase();
        HttpStatus status = (low.contains("not found") || low.contains("no plan") || low.contains("not_found"))
                ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setDetail(msg);
        log.warn("IllegalArgumentException: {} -> status={}", msg, status);
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        String detailMsg = errors.isEmpty()
            ? "validation_error"
            : "Validation failed: " + errors.entrySet().stream()
                .map(e -> e.getKey() + " - " + e.getValue())
                .collect(Collectors.joining(", "));

        pd.setDetail(detailMsg);
        pd.setProperty("errors", errors);

        log.warn("Validation failed: {}", errors);
        return pd;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMessageNotReadable(HttpMessageNotReadableException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        String detailMsg = "Invalid request body";
        Throwable cause = ex.getCause();

        // Se for erro de parsing de enum ou tipo
        if (cause instanceof InvalidFormatException ife) {
            String fieldName = ife.getPath().stream()
                .map(ref -> ref.getFieldName())
                .filter(name -> name != null)
                .collect(Collectors.joining("."));

            String targetType = ife.getTargetType().getSimpleName();
            Object value = ife.getValue();

            detailMsg = String.format(
                "Invalid value '%s' for field '%s'. Expected type: %s",
                value, fieldName, targetType
            );

            if (ife.getTargetType().isEnum()) {
                Object[] enumConstants = ife.getTargetType().getEnumConstants();
                String validValues = java.util.Arrays.stream(enumConstants)
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
                detailMsg += ". Valid values: [" + validValues + "]";
            }
        }

        pd.setDetail(detailMsg);
        log.error("Message not readable: {} | Cause: {}", detailMsg, ex.getMessage());
        return pd;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "illegal_state";
        HttpStatus status;
        if (msg.contains("patient_service_unavailable")) {
            status = HttpStatus.SERVICE_UNAVAILABLE; // 503
        } else if (msg.contains("patient_service_error")) {
            status = HttpStatus.BAD_GATEWAY; // 502
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setDetail(msg);
        log.error("IllegalStateException: {} -> status={}", msg, status);
        return pd;
    }
}
