package com.kdongd.shortari.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ShortUrlNotFoundException extends RuntimeException {

    public ShortUrlNotFoundException() {
        super("요청한 URL을 찾을 수 없습니다.");
    }
}