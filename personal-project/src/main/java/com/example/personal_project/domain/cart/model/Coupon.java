package com.example.personal_project.domain.cart.model;

import com.example.personal_project.global.exception.InvalidCouponException;

public enum Coupon {
    NONE, PERCENT10, FIXED15000;

    public static Coupon from(String code) {
        if (code == null) return NONE;
        try {
            return valueOf(code);
        } catch (IllegalArgumentException exception) {
            throw new InvalidCouponException("알 수 없는 쿠폰입니다: " + code);
        }
    }
}
