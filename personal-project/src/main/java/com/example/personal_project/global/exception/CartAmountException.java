package com.example.personal_project.global.exception;

public class CartAmountException extends IllegalArgumentException {
    public CartAmountException(String message, Throwable cause) {
        super(message, cause);
    }
}
