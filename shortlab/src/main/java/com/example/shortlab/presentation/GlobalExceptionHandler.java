package com.example.shortlab.presentation;

import com.example.shortlab.domain.exception.DuplicateShortenUrlKeyException;
import com.example.shortlab.domain.exception.LackOfShortenUrlKeyException;
import com.example.shortlab.domain.exception.NotFoundShortenUrlException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundShortenUrlException.class)
    public ResponseEntity<String> handleNotFoundShortenUrlException(
            NotFoundShortenUrlException exception)
    {
        return new ResponseEntity<>("단축 URL을 찾지 못했습니다.", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(LackOfShortenUrlKeyException.class)
    public ResponseEntity<String> handleLackOfShortenUrlKeyException(
            LackOfShortenUrlKeyException exception)
    {
        return new ResponseEntity<>("단축 URL 자원이 부족합니다.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DuplicateShortenUrlKeyException.class)
    public ResponseEntity<String> handleDuplicateShortenUrlKeyException(
            DuplicateShortenUrlKeyException exception)
    {
        return new ResponseEntity<>("이미 사용 중이거나 사용할 수 없는 별칭입니다.", HttpStatus.CONFLICT);
    }
}
