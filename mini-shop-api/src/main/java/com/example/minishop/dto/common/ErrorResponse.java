package com.example.minishop.dto.common;

import com.fasterxml.jackson.annotation.JacksonInject;

import java.time.LocalDateTime;
import java.util.List;

public class ErrorResponse {

    private boolean success;
    private String code;
    private String message;
    private List<FieldErrorResponse> errors;
    private LocalDateTime timestamp;

    public ErrorResponse(
            boolean success,
            String code,
            String message,
            List<FieldErrorResponse> errors
    ) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.errors = errors;
        this.timestamp = LocalDateTime.now();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<FieldErrorResponse> getErrors() {
        return errors;
    }

    public void setErrors(List<FieldErrorResponse> errors) {
        this.errors = errors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}