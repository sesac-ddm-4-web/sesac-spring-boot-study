package com.example.personal_project.domain.exception;

public class CommonNotFoundException extends RuntimeException {
    public CommonNotFoundException(String message) {
        super(message);
    }
}
