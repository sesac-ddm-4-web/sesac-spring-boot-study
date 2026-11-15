package com.work.shortenurl.domain;

import com.work.shortenurl.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        int status,
        String code,
        String message,
        List<FieldErrorDetail> errors,
        LocalDateTime timestamp
) {
    public record FieldErrorDetail(String field, String reason) {
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return of(errorCode, message, List.of());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, List<FieldErrorDetail> errors) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.name(),
                message,
                errors,
                LocalDateTime.now()
        );
    }
}
