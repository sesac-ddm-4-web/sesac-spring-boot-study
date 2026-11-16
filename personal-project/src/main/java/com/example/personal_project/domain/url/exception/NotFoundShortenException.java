package com.example.personal_project.domain.url.exception;



public class NotFoundShortenException extends RuntimeException {
    public NotFoundShortenException(String message) {
        super(message);
    }
}
