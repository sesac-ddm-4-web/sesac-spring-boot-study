package com.example.shorturl.global.exception;

public record ErrorRes(int status, String code, String message) {

    public static ErrorRes of(ErrorCode errorCode, String message) {
        return new ErrorRes(errorCode.getStatus().value(), errorCode.name(), message);
    }
}
