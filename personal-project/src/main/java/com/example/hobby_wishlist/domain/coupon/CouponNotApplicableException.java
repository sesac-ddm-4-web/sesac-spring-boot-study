package com.example.hobby_wishlist.domain.coupon;

import com.example.hobby_wishlist.domain.ConflictException;

public class CouponNotApplicableException extends ConflictException {
    public CouponNotApplicableException(String message) {
        super(message);
    }
}
