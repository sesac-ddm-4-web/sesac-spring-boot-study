package com.sesac.studyhub.global.exception;

import java.util.List;
import org.springframework.validation.FieldError;

public record ErrorResponse(
    int status,
    String code,
    String message,
    List<FieldErrorDetail> errors
) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getStatus().value(), errorCode.name(), errorCode.getMessage(), List.of());
    }

    public static ErrorResponse of(ErrorCode errorCode, List<FieldError> fieldErrors) {
        List<FieldErrorDetail> details = fieldErrors.stream()
            .map(e -> new FieldErrorDetail(e.getField(), e.getDefaultMessage()))
            .toList();

        return new ErrorResponse(errorCode.getStatus().value(), errorCode.name(),
            errorCode.getMessage(), details);
    }

    public record FieldErrorDetail(String field, String reason) {}
}
