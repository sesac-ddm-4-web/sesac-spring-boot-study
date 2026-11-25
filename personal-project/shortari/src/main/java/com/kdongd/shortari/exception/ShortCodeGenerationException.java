package com.kdongd.shortari.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ShortCodeGenerationException extends RuntimeException {

    public ShortCodeGenerationException() {
        super("단축 URL 코드 생성에 실패했습니다.");
    }
}