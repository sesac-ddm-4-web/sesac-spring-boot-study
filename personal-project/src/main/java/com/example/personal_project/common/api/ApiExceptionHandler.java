package com.example.personal_project.common.api;

import com.example.personal_project.common.InvalidDomainFieldException;
import com.example.personal_project.common.application.ServiceException;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponse> handleServiceException(ServiceException exception) {
        HttpStatus status = switch (exception.getCode()) {
            case ACTOR_REQUIRED -> HttpStatus.BAD_REQUEST;
            case OWNER_REQUIRED, ACTIVE_MEMBERSHIP_REQUIRED -> HttpStatus.FORBIDDEN;
            case MEMBER_NOT_FOUND, STUDY_NOT_FOUND, MEMBERSHIP_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case EMAIL_ALREADY_EXISTS, ALREADY_JOINED, REJOIN_NOT_ALLOWED, RECRUITMENT_CLOSED,
                    STUDY_FULL, OWNER_CANNOT_LEAVE -> HttpStatus.CONFLICT;
        };
        return ResponseEntity.status(status)
                .body(new ErrorResponse(exception.getCode().name(), exception.getMessage(), null));
    }

    @ExceptionHandler(InvalidDomainFieldException.class)
    public ResponseEntity<ErrorResponse> handleInvalidField(InvalidDomainFieldException exception) {
        return invalidRequest(List.of(new FieldError(exception.getField(), exception.getMessage())));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBody() {
        return invalidRequest(null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleInvalidArgument(MethodArgumentTypeMismatchException exception) {
        String field = exception.getName();
        return invalidRequest(List.of(new FieldError(field, field + " has an invalid value")));
    }

    @ExceptionHandler({PessimisticLockingFailureException.class, LockTimeoutException.class, PessimisticLockException.class})
    public ResponseEntity<ErrorResponse> handleLockConflict() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("TEMPORARY_CONFLICT", "다른 요청이 처리 중입니다. 잠시 후 다시 요청해 주세요.", null));
    }

    private static ResponseEntity<ErrorResponse> invalidRequest(List<FieldError> fields) {
        return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_REQUEST", "요청 값이 올바르지 않습니다.", fields));
    }

    public record ErrorResponse(String code, String message, @JsonInclude(JsonInclude.Include.NON_NULL) List<FieldError> fieldErrors) {
    }

    public record FieldError(String field, String message) {
    }
}
