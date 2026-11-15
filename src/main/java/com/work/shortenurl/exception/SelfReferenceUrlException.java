package com.work.shortenurl.exception;

public class SelfReferenceUrlException extends BusinessException {
    public SelfReferenceUrlException() {
        super(ErrorCode.SELF_REFERENCE_URL);
    }
}