package com.example.demo.global.exception;

// 같은 외부 상품을 다시 가져올 때, 가져온 외부 상품을 수정하려 할 때 등
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
