package com.example.personal_project.presentation;

import com.example.personal_project.domain.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 여러 Controller에 공통으로 적용할 예외 처리 클래스 지정
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 수량·재고·상품 종류 제한 충돌 - 409
    // 이 종류의 예외가 발생하면 이 메서드가 처리
    @ExceptionHandler(IllegalStateException.class)
    // HTTP 응답 상태를 409로 지정
    @ResponseStatus(HttpStatus.CONFLICT)
    String handleConflict(IllegalStateException exception) {
        return exception.getMessage();
//        Cart에서 수량 초과 예외 발생
//        → 핸들러가 예외를 받음
//        → 409 상태와 예외 메시지를 응답
    }

    // 없는 상품 - 404
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String handleNotFound (EntityNotFoundException exception) {
        return exception.getMessage();
    }

    // 잘못된 입력 - 400
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String handleBadRequest (IllegalArgumentException exception) {
        return exception.getMessage();
    }
}
