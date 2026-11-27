package com.example.study.common.exception;

/**
 * 서비스 규칙을 어겼을 때 던진다. RuntimeException이므로 트랜잭션이 롤백된다.
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
