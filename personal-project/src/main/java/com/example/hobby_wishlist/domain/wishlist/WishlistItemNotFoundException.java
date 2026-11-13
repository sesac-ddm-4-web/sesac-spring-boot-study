package com.example.hobby_wishlist.domain.wishlist;

import com.example.hobby_wishlist.domain.ItemNotFoundException;

public class WishlistItemNotFoundException extends ItemNotFoundException {
    public WishlistItemNotFoundException(String message) {
        super(message);
    }
}
