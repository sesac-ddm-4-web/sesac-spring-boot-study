package com.example.wishlist.management.domain.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
