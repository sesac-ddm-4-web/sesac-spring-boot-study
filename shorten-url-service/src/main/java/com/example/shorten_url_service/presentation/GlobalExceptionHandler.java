package com.example.shorten_url_service.presentation;

import com.example.shorten_url_service.domain.LackOfShortenUrlKeyException;
import com.example.shorten_url_service.domain.NotFoundShortenUrlException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LackOfShortenUrlKeyException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleLackOfShortenUrlKeyException(
    ) {
        return "단축 URL 자원이 부족합니다.";
    }

    @ExceptionHandler(NotFoundShortenUrlException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundShortenUrlException() {
        return "단축 URL을 찾지 못했습니다.";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.CONTENT_TOO_LARGE)
    public String handleMaxUploadSizeExceededException() {
        return "PDF는 최대 10MB까지 업로드할 수 있습니다.";
    }
}
