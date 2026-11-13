package com.example.hobby_wishlist.domain.coupon;

import com.example.hobby_wishlist.domain.ItemNotFoundException;

public class CouponNotFoundException extends ItemNotFoundException {
    public CouponNotFoundException(String message) {
        super(message);
    }
}
