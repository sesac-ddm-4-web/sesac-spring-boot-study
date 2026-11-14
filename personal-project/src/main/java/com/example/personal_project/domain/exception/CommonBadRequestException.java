package com.example.personal_project.domain.exception;

public class CommonBadRequestException extends RuntimeException {
    public CommonBadRequestException(String message) {
        super(message);
    }
}
