package com.example.hobby_wishlist.domain.cart;

import com.example.hobby_wishlist.domain.ConflictException;

public class CartRuleViolationException extends ConflictException {
    public CartRuleViolationException(String message) {
        super(message);
    }
}
