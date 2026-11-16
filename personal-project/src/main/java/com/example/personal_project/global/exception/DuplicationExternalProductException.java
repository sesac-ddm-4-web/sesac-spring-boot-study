package com.example.personal_project.global.exception;

public class DuplicationExternalProductException extends RuntimeException {
    public DuplicationExternalProductException() {
        super("이미 위시리스트에 가져온 외부 상품입니다.");
    }
}
