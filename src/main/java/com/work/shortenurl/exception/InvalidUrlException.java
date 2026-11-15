package com.work.shortenurl.exception;

public class InvalidUrlException extends BusinessException {
    public InvalidUrlException(String message) {
        super(ErrorCode.INVALID_URL, message);
    }
}
