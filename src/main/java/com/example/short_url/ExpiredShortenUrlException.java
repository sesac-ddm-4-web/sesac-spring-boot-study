package com.example.short_url;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GONE)
public class ExpiredShortenUrlException extends RuntimeException {
    //public ExpiredShortenUrlException(String message) { super(message); }
}
