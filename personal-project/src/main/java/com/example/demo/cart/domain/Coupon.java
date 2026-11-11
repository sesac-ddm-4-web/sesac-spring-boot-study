package com.example.demo.cart.domain;

import com.example.demo.global.exception.BadRequestException;

public enum Coupon {
    NONE,
    PERCENT10,
    FIXED15000;

    private static final long PERCENT10_MAX_DISCOUNT = 20_000;
    private static final long FIXED15000_MIN_SUBTOTAL = 100_000;
    private static final long FIXED15000_DISCOUNT = 15_000;

    public boolean isApplicable(long subtotal) {
        return switch (this) {
            case NONE -> true; // 항상 가능
            case PERCENT10 -> subtotal > 0; // 담긴 상품이 있으면
            case FIXED15000 -> subtotal >= FIXED15000_MIN_SUBTOTAL; // 상품 합계 100,000원 이상
        };
    }

    public long discount(long subtotal) {
        return switch (this) {
            case NONE -> 0;
            case PERCENT10 -> Math.min(subtotal / 10, PERCENT10_MAX_DISCOUNT);
            case FIXED15000 -> FIXED15000_DISCOUNT;
        };
    }

    public static Coupon from(String code) {
        if (code == null || code.isBlank()) {
            return NONE;
        }
        try {
            return Coupon.valueOf(code.strip());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("알 수 없는 쿠폰입니다: " + code);
        }
    }
}
