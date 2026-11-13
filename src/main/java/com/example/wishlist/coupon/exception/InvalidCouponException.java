package com.example.wishlist.coupon.exception;

import com.example.wishlist.common.exception.BadRequestException;

public class InvalidCouponException extends BadRequestException {

    public InvalidCouponException(String message) {
        super(message);
    }
}