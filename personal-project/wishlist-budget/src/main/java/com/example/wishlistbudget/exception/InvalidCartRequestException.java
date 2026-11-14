package com.example.wishlistbudget.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidCartRequestException extends RuntimeException {

    public InvalidCartRequestException(String message) {
        super(message);
    }
}