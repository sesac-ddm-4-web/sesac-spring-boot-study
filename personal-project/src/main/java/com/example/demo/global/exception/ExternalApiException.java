package com.example.demo.global.exception;

// 외부 api 호출 실패시 예외
public class ExternalApiException extends RuntimeException{
    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
