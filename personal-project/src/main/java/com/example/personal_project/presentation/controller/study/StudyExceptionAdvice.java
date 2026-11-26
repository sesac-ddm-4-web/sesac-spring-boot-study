package com.example.personal_project.presentation.controller.study;

import com.example.personal_project.domain.study.exception.StudyException;
import com.example.personal_project.domain.url.exception.DuplicateShortenUrlCodeException;
import com.example.personal_project.domain.url.exception.LackOfShortenUrlKeyException;
import java.util.Map;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages="com.example.personal_project.presentation.controller.study")
public class StudyExceptionAdvice {
    @ExceptionHandler(StudyException.class) public ResponseEntity<?> domain(StudyException e) { int status = switch (e.reason()) {
            case NOT_FOUND -> 404;
            case FORBIDDEN -> 403;
            case INVALID_PRESENTER -> 400;
            case DUPLICATE_SESSION_NO -> 409;
        };
        return error(status, e.reason().name(), e.getMessage()); }
    @ExceptionHandler(DuplicateShortenUrlCodeException.class) public ResponseEntity<?> code(Exception e) { return error(409,"DUPLICATE_CODE",e.getMessage()); }
    @ExceptionHandler(DuplicateKeyException.class) public ResponseEntity<?> duplicate(Exception e) {
        return error(500,"DATABASE_ERROR","저장 작업을 완료하지 못했습니다.");
    }
    @ExceptionHandler(LackOfShortenUrlKeyException.class) public ResponseEntity<?> exhausted(Exception e) { return error(500,"URL_CREATE_FAILED",e.getMessage()); }
    @ExceptionHandler({IllegalArgumentException.class,MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,MissingServletRequestParameterException.class,HttpMessageNotReadableException.class})
    public ResponseEntity<?> invalid(Exception e) {
        return error(400,"INVALID_INPUT",e instanceof IllegalArgumentException ? e.getMessage():"입력값의 형식과 필수 항목을 확인해주세요.");
    }
    private ResponseEntity<?> error(int status,String code,String message) { return ResponseEntity.status(status).body(Map.of("code",code,"message",message)); }
}
