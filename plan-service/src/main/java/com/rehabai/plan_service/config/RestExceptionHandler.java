package com.rehabai.plan_service.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "bad_request";
        HttpStatus status = (msg.toLowerCase().contains("not found") || msg.toLowerCase().contains("no plan"))
                ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setDetail(msg);
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setDetail("validation_error");
        return pd;
    }
}

