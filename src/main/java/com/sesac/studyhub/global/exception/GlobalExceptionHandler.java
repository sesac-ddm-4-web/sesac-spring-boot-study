package com.sesac.studyhub.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 서비스 규칙 위반
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
        ErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code));
    }

    // valid 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(ErrorResponse.of(ErrorCode.INVALID_INPUT, e.getBindingResult().getFieldErrors()));
    }

    // JSON 형식 오류, PathVariable 오류
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception e) {
        return ResponseEntity.badRequest().body(ErrorResponse.of(ErrorCode.INVALID_INPUT));
    }

    // 동시 요청으로 서비스 검사를 통과한 중복 참여 => DB 유니크 제약이 최종 방어
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("데이터 무결성 위반: {}", e.getMostSpecificCause().getMessage());
        return ResponseEntity.status(ErrorCode.ALREADY_JOINED.getStatus())
            .body(ErrorResponse.of(ErrorCode.ALREADY_JOINED));
    }

    // 그 외 예상하지 못한 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.internalServerError().body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    // security 관련
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException e) {
        return ResponseEntity.status(ErrorCode.RESOURCE_NOT_FOUND.getStatus())
            .body(ErrorResponse.of(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
        HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.getStatus())
            .body(ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED));
    }

}
