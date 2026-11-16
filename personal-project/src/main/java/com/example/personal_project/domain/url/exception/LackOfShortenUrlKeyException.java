package com.example.personal_project.domain.url.exception;



public class LackOfShortenUrlKeyException extends RuntimeException {
    public LackOfShortenUrlKeyException(String message) {
        super(message);
    }
}
