package com.example.wishlist.management.presentation.common;

import com.example.wishlist.management.domain.exception.ConflictException;
import com.example.wishlist.management.domain.exception.ExternalApiException;
import com.example.wishlist.management.domain.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // @RequestParam, @PathVariable같은 개별 파라미터 검증 실패
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorMessage> handleHandlerMethodValidation(
            HandlerMethodValidationException e) {
        List<String> errors = e.getParameterValidationResults().stream()
                .flatMap(r -> r.getResolvableErrors().stream()
                        .map(err -> r.getMethodParameter().getParameterName()
                                + ": " + err.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(new ErrorMessage(errors));
    }

    // @Valid @RequestBody로 받은 객체를 검사
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest().body(new ErrorMessage(errors));
    }

    // Wish 생성자 검증 실패
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorMessage> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ErrorMessage(List.of(e.getMessage())));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorMessage> handleNotFound(NotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorMessage> handleConflict(ConflictException e) {
        return error(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorMessage> handleExternalApi(ExternalApiException e) {
        return error(HttpStatus.BAD_GATEWAY, e.getMessage());   // 502
    }

    private ResponseEntity<ErrorMessage> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorMessage(List.of(message)));
    }
}
