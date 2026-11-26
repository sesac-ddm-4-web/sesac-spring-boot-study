package com.example.shorturl.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorRes> handleApiException(ApiException ex) {
        return error(ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRes> handleNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining(", "));
        return error(ErrorCode.INVALID_INPUT, ErrorCode.INVALID_INPUT.format(message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRes> handleNotReadableException(HttpMessageNotReadableException ex) {
        return error(ErrorCode.INVALID_REQUEST_BODY, ErrorCode.INVALID_REQUEST_BODY.format());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorRes> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        return error(ErrorCode.INVALID_PARAMETER_TYPE, ErrorCode.INVALID_PARAMETER_TYPE.format(ex.getName()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorRes> handleMissingParameterException(MissingServletRequestParameterException ex) {
        return error(ErrorCode.MISSING_PARAMETER, ErrorCode.MISSING_PARAMETER.format(ex.getParameterName()));
    }

    private ResponseEntity<ErrorRes> error(ErrorCode errorCode, String message) {
        return ResponseEntity.status(errorCode.getStatus()).body(ErrorRes.of(errorCode, message));
    }
}
