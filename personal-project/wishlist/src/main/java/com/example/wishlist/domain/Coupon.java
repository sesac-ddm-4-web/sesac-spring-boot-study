package com.example.wishlist.domain;

public enum Coupon {
    NONE,
    PERCENT10,
    FIXED15000;

    private static final long PERCENT10_MAX_DISCOUNT = 20_000;
    private static final long FIXED_DISCOUNT = 15_000;
    private static final long FIXED_MIN_SUBTOTAL = 100_000;

    public static Coupon from(String code) {
        if (code == null || code.isBlank()) {
            return NONE;
        }
        for (Coupon coupon : values()) {
            if (coupon.name().equals(code.strip())) {
                return coupon;
            }
        }
        throw new IllegalArgumentException("알 수 없는 쿠폰입니다. (" + code + ")");
    }

    public long discount(long subtotal) {
        if (this != NONE && subtotal == 0) {
            throw new IllegalArgumentException("빈 장바구니에는 쿠폰을 사용할 수 없습니다.");
        }

        if (this == PERCENT10) {
            long discount = subtotal * 10 / 100;
            return Math.min(discount, PERCENT10_MAX_DISCOUNT);
        }
        if (this == FIXED15000) {
            if (subtotal < FIXED_MIN_SUBTOTAL) {
                throw new IllegalArgumentException(
                        "FIXED15000 쿠폰은 상품 합계가 100,000원 이상일 때만 사용할 수 있습니다.");
            }
            return FIXED_DISCOUNT;
        }
        return 0;
    }
}
