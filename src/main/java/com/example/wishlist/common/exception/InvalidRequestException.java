package com.example.wishlist.common.exception;

public class InvalidRequestException extends BadRequestException {

    public InvalidRequestException(String message) {
        super(message);
    }
}