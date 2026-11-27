package com.example.study.common.exception;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 모든 오류를 {@link ErrorResponse} 형식으로 변환한다.
 * Spring MVC가 던지는 예외는 부모 클래스가 상태 코드를 정하고, 여기서는 본문만 바꾼다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
        ErrorCode code = e.errorCode();
        return ResponseEntity.status(code.status())
                .body(ErrorResponse.of(code.status().value(), code.message()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        // 내부 정보는 응답에 담지 않고 로그에만 남긴다.
        log.error("처리하지 못한 예외", e);
        ErrorCode code = ErrorCode.INTERNAL_ERROR;
        return ResponseEntity.status(code.status())
                .body(ErrorResponse.of(code.status().value(), code.message()));
    }

    /** {@code @Valid @RequestBody} 검증 실패 */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ErrorResponse.FieldError> errors = e.getBindingResult().getAllErrors().stream()
                .map(error -> new ErrorResponse.FieldError(
                        error instanceof FieldError field ? field.getField() : error.getObjectName(),
                        error.getDefaultMessage()))
                .toList();
        return validationFailure(e, errors, headers, status, request);
    }

    /** 경로 변수·요청 파라미터에 붙인 제약 조건의 검증 실패 */
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        // 반환값 검증 실패(500)는 입력값 오류가 아니다.
        if (!status.is4xxClientError()) {
            return handleExceptionInternal(e, null, headers, status, request);
        }
        List<ErrorResponse.FieldError> errors = e.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map((MessageSourceResolvable error) -> new ErrorResponse.FieldError(
                                result.getMethodParameter().getParameterName(),
                                error.getDefaultMessage())))
                .toList();
        return validationFailure(e, errors, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception e, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        if (status.is5xxServerError()) {
            log.error("요청 처리 중 서버 오류", e);
        }
        Object response = body instanceof ErrorResponse
                ? body
                : ErrorResponse.of(status.value(), messageFor(status));
        return super.handleExceptionInternal(e, response, headers, status, request);
    }

    private ResponseEntity<Object> validationFailure(
            Exception e, List<ErrorResponse.FieldError> errors,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ErrorResponse body = new ErrorResponse(status.value(), ErrorCode.INVALID_INPUT.message(), errors);
        return handleExceptionInternal(e, body, headers, status, request);
    }

    private String messageFor(HttpStatusCode status) {
        for (ErrorCode code : ErrorCode.values()) {
            if (code.status().value() == status.value()) {
                return code.message();
            }
        }
        return status.is4xxClientError() ? "잘못된 요청입니다." : ErrorCode.INTERNAL_ERROR.message();
    }
}
