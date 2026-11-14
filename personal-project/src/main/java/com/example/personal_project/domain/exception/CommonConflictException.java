package com.example.personal_project.domain.exception;

public class CommonConflictException extends RuntimeException {
    public CommonConflictException(String message) {
        super(message);
    }
}
