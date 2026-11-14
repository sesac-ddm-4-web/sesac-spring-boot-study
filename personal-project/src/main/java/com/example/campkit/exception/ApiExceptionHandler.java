
package com.example.campkit.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private ResponseEntity<Map<String, String>> error(
            HttpStatus status, String message) {

        return ResponseEntity.status(status)
                .body(Map.of("error", message));
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<Map<String, String>> badRequest(Exception e) {

        if (e instanceof MethodArgumentNotValidException validation) {
            String message = validation.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .findFirst()
                    .map(f -> f.getDefaultMessage())
                    .orElse("입력값이 올바르지 않습니다.");

            return error(HttpStatus.BAD_REQUEST, message);
        }

        String message = e instanceof IllegalArgumentException
                ? e.getMessage()
                : "입력값이 올바르지 않습니다.";

        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(
            NotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> conflict(
            ConflictException e) {
        return error(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<Map<String, String>> externalError(
            ExternalApiException e) {
        return error(HttpStatus.BAD_GATEWAY, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> serverError(Exception e) {
        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "서버 오류가 발생했습니다."
        );
    }
}
