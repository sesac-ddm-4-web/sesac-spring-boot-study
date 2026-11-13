package com.example.hobby_wishlist.domain;

public abstract class ItemNotFoundException extends RuntimeException {
    protected ItemNotFoundException(String message) {
        super(message);
    }
}
