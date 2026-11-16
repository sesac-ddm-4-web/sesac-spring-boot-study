package com.example.personal_project.global.exception.handler;

import com.example.personal_project.global.exception.*;
import com.example.personal_project.global.exception.dto.ErrorResponse;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDatabase(DataAccessException exception) {
        log.error("DB 작업 실패: 테이블 구조와 SQL을 확인하세요.", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "DATABASE_ERROR",
                "DB 작업에 실패했습니다. 서버 콘솔의 오류 로그를 확인해주세요.");
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInput(IllegalArgumentException exception) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        String message = "입력값을 확인해주세요.";
        var fieldError = exception.getBindingResult().getFieldError();
        if (fieldError != null && fieldError.getDefaultMessage() != null) {
            message = fieldError.getDefaultMessage();
        }
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message);
    }

    @ExceptionHandler(InvalidCouponException.class)
    public ResponseEntity<ErrorResponse> handleCoupon(InvalidCouponException exception) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_COUPON", exception.getMessage());
    }

    @ExceptionHandler(CartAmountException.class)
    public ResponseEntity<ErrorResponse> handleAmount(CartAmountException exception) {
        return error(HttpStatus.BAD_REQUEST, "CART_AMOUNT_OVERFLOW", exception.getMessage());
    }

    @ExceptionHandler({MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> handleMalformedRequest(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청 파라미터 또는 본문의 형식을 확인해주세요.");
    }

    @ExceptionHandler(DuplicationExternalProductException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicationExternalProductException exception) {
        return error(HttpStatus.CONFLICT, "DUPLICATE_EXTERNAL_PRODUCT", exception.getMessage());
    }

    @ExceptionHandler(CartConflictException.class)
    public ResponseEntity<ErrorResponse> handleCartConflict(CartConflictException exception) {
        return error(HttpStatus.CONFLICT, "CART_CONFLICT", exception.getMessage());
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCartNotFound(CartItemNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "CART_ITEM_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException exception) {
        return error(HttpStatus.NOT_FOUND, "ITEM_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(ExternalProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleExternalNotFound(ExternalProductNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "EXTERNAL_PRODUCT_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponse> handleExternalApi(ExternalApiException exception) {
        return error(HttpStatus.BAD_GATEWAY, "EXTERNAL_API_FAILURE", "외부 상품 서비스의 응답을 처리할 수 없습니다.");
    }

    @ExceptionHandler(NotFoundShortenException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundShortenUrlException(NotFoundShortenException exception) {
        return error(HttpStatus.NOT_FOUND, "URL_NOT_FOUND", "단축 URL을 찾지 못햇습니다.");
    }

    @ExceptionHandler(LackOfShortenUrlKeyException.class)
    public ResponseEntity<ErrorResponse> handleLackOfShortenUrlKeyException(LackOfShortenUrlKeyException exception) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "URL_CREATE_FAILED", "단축 URL 자원이 부족합니다.");
    }

    @ExceptionHandler(DuplicateShortenUrlCodeException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateShortenCodeException(DuplicateShortenUrlCodeException exception) {
        return error(HttpStatus.CONFLICT, "DUPLICATE_CODE", exception.getMessage());
    }
}
