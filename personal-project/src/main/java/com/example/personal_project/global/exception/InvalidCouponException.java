package com.example.personal_project.global.exception;

public class InvalidCouponException extends IllegalArgumentException {
    public InvalidCouponException(String message) {
        super(message);
    }
}
