package com.example.personal_project.global.exception;

public class DuplicateShortenUrlCodeException extends RuntimeException {
    public DuplicateShortenUrlCodeException(String message) {
        super(message);
    }
}
