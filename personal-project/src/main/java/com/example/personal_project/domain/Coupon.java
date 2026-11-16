package com.example.personal_project.domain;

// 쿠폰 종류
public enum Coupon {
    NONE,
    PERCENT10,
    FIXED15000;

    public long calculateDiscount(long subtotal) {
        if (this == PERCENT10) {
            // 10% 할인, 원 미만 버림, 최대 20000
            return Math.min(subtotal/10, 20000L);
        }

        if (this == FIXED15000) {
            // 10만원 미만
            if (subtotal < 100000L) {
                throw new IllegalArgumentException(
                        "FIXED15000 쿠폰은 상품 합계 100,000원 이상일 때 사용 가능합니다."
                );
            }
            // 10만원 이상
            return 15000L;
        }
        return 0L;
    }
}
