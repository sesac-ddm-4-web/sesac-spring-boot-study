package com.example.personal_project.study;

import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DuplicateKeyException;
import com.example.personal_project.global.exception.*;
import java.util.Map;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes={StudyApiController.class,StudyAuthController.class})
public class StudyExceptionAdvice {
    @ExceptionHandler(StudyException.class) public ResponseEntity<?> domain(StudyException e) { return error(e.status,e.code,e.getMessage()); }
    @ExceptionHandler(DuplicateShortenUrlCodeException.class) public ResponseEntity<?> code(Exception e) { return error(409,"DUPLICATE_CODE",e.getMessage()); }
    @ExceptionHandler(DuplicateKeyException.class) public ResponseEntity<?> duplicate(Exception e) {
        Throwable cause=((DuplicateKeyException)e).getMostSpecificCause();
        if(cause.getMessage()!=null && cause.getMessage().contains("uk_study_sessions_no"))
            return error(409,"DUPLICATE_SESSION_NO","이미 등록된 회차 번호입니다.");
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
