package com.example.personal_project.global.exception;

public class ExternalProductNotFoundException extends RuntimeException {
    public ExternalProductNotFoundException(Long id) { super("외부 상품을 찾을 수 없습니다. ID: " + id); }
}
