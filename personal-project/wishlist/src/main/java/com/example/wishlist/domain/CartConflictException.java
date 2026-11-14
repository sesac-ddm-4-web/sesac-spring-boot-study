package com.example.wishlist.domain;

public class CartConflictException extends RuntimeException {
    public CartConflictException(String message) {
        super(message);
    }
}
