package com.example.demo.global.exception;

// 규칙에 안맞는 조건
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
