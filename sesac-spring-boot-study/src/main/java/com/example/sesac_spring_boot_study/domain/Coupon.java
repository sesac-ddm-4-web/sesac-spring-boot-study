package com.example.sesac_spring_boot_study.domain;

public enum Coupon {
    NONE {
        @Override
        public long discount(long subtotal) {
            return 0;
        }
    },
    PERCENT10 {
        @Override
        public long discount(long subtotal) {
            return Math.min(subtotal * 10 / 100, 20000);
        }
    },
    FIXED15000 {
        @Override
        public long discount(long subtotal) {
            if (subtotal < 100_000) {
                throw new IllegalArgumentException("상품 합계가 100,000원 이상일 때만 사용할 수 있습니다.");
            }
            return 15000;
        }
    };

    public abstract long discount(long subtotal);

    public static Coupon from(String code) {
        if (code == null || code.isBlank()) {
            return NONE;
        }
        try {
            return valueOf(code.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("알 수 없는 쿠폰입니다: " + code);
        }
    }
}