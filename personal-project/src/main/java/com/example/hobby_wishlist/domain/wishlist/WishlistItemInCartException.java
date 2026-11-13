package com.example.hobby_wishlist.domain.wishlist;

import com.example.hobby_wishlist.domain.ConflictException;

public class WishlistItemInCartException extends ConflictException {
    public WishlistItemInCartException(String message) {
        super(message);
    }
}
