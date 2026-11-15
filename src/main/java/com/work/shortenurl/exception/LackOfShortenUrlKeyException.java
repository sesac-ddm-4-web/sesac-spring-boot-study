package com.work.shortenurl.exception;

public class LackOfShortenUrlKeyException extends BusinessException {
    public LackOfShortenUrlKeyException() {
        super(ErrorCode.KEY_GENERATION_FAILED);
    }
}