
package com.example.campkit.domain;

public enum Coupon {

    NONE {
        @Override
        public long calculateDiscount(long subtotal) {
            return 0;
        }
    },

    PERCENT10 {
        @Override
        public long calculateDiscount(long subtotal) {
            return Math.min(subtotal / 10, 20_000L);
        }
    },

    FIXED15000 {
        @Override
        public long calculateDiscount(long subtotal) {
            if (subtotal < 100_000L) {
                throw new IllegalArgumentException(
                        "상품 합계 100,000원 이상에서 사용할 수 있습니다."
                );
            }
            return 15_000L;
        }
    };

    public abstract long calculateDiscount(long subtotal);

    public static Coupon from(String code) {
        try {
            return Coupon.valueOf(
                    code == null ? "NONE" : code
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "존재하지 않는 쿠폰입니다."
            );
        }
    }
}
