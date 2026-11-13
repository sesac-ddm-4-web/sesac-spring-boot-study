package com.example.hobby_wishlist.domain.cart;

import com.example.hobby_wishlist.domain.ItemNotFoundException;

public class CartItemNotFoundException extends ItemNotFoundException {
    public CartItemNotFoundException(String message) {
        super(message);
    }
}
