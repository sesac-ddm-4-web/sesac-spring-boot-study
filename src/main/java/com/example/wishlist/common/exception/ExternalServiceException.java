package com.example.wishlist.common.exception;

public abstract class ExternalServiceException extends RuntimeException {

    protected ExternalServiceException(String message) {
        super(message);
    }
}