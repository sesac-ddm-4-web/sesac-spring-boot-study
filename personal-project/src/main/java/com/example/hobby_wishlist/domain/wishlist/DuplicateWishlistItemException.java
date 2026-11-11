package com.example.hobby_wishlist.domain.wishlist;

import com.example.hobby_wishlist.domain.ConflictException;

public class DuplicateWishlistItemException extends ConflictException {
    public DuplicateWishlistItemException(String message) {
        super(message);
    }
}
