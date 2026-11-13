package com.example.wishlist.cart.exception;

import com.example.wishlist.common.exception.ConflictException;

public class CartRuleViolationException extends ConflictException {

    public CartRuleViolationException(String message) {
        super(message);
    }
}