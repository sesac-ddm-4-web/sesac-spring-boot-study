package com.example.personal_project.domain.url.exception;



public class DuplicateShortenUrlCodeException extends RuntimeException {
    public DuplicateShortenUrlCodeException(String message) {
        super(message);
    }
}
