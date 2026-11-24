package com.example.short_url.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundShortenUrlException.class)
    public ResponseEntity<String> handleNotFooundShortenUrlException(NotFoundShortenUrlException ex){
        return new ResponseEntity<>("단축 URL을 찾지 못했습니다.", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(LackOfShortenUrlKeyException.class)
    public ResponseEntity<String> handleLackOfshortenUrlKeyException(LackOfShortenUrlKeyException ex){
        return new ResponseEntity<>("단축 URL 자원이 부족합니다.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DuplicateShortenUrlKeyException.class)
    public ResponseEntity<String> handleDuplicateShortenUrlKeyException(DuplicateShortenUrlKeyException ex){
        return new ResponseEntity<>("이미 사용 중인 주소입니다.", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ExpiredShortenUrlException.class)
    public ResponseEntity<String> handleExpiredShortenUrlException(ExpiredShortenUrlException ex){
        return new ResponseEntity<>("만료된 단축 URL입니다.", HttpStatus.GONE);
    }

    @ExceptionHandler(InvalidManagementTokenException.class)
    public ResponseEntity<String> handleInvalidManagementTokenException(InvalidManagementTokenException ex){
        return new ResponseEntity<>("관리 토큰이 올바르지 않습니다.", HttpStatus.FORBIDDEN);
    }

}
