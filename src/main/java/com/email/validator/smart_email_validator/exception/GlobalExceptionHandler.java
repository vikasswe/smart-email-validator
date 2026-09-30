package com.email.validator.smart_email_validator.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadableBody(
            HttpMessageNotReadableException ex
    ) {
        Throwable cause = ex.getMostSpecificCause();

        return ResponseEntity.badRequest().body(
                Map.of(
                        "error", "Invalid request body",
                        "details", cause.getMessage() == null
                                ? "Unable to read request body"
                                : cause.getMessage()
                )
        );
    }

}
