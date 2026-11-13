package com.example.wishlist.management.domain.exception;

public class ExternalApiException extends RuntimeException {
    public ExternalApiException(String message, Throwable cause) { super(message, cause); }
}
