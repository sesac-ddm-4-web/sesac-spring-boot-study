package com.example.wishlist.coupon.domain;

import com.example.wishlist.coupon.exception.InvalidCouponException;
import java.util.Locale;

public enum Coupon {

    /** 할인 없음 — 언제나 사용 가능 */
    NONE("언제나") {
        @Override
        public boolean isApplicableTo(long subtotal) {
            return true;
        }

        @Override
        public long discountFor(long subtotal) {
            return 0L;
        }
    },

    /** 10% 할인 · 원 미만 버림 · 최대 20,000원 */
    PERCENT10("상품이 담겨 있을 때") {
        @Override
        public boolean isApplicableTo(long subtotal) {
            return subtotal > 0;
        }

        @Override
        public long discountFor(long subtotal) {
            long tenPercent = subtotal * 10 / 100;   // 정수 나눗셈 → 원 미만 버림
            return Math.min(tenPercent, 20_000L);
        }
    },

    /** 상품 합계가 100,000원 이상일 때 15,000원 할인 */
    FIXED15000("상품 합계가 100,000원 이상일 때") {
        @Override
        public boolean isApplicableTo(long subtotal) {
            return subtotal >= 100_000L;
        }

        @Override
        public long discountFor(long subtotal) {
            return 15_000L;
        }
    };

    private final String condition;

    Coupon(String condition) {
        this.condition = condition;
    }

    public abstract boolean isApplicableTo(long subtotal);

    public abstract long discountFor(long subtotal);

    /** 사용 조건 설명 (사용할 수 없을 때 안내 문구에 사용) */
    public String condition() {
        return condition;
    }

    /**
     * 요청 값 → 쿠폰
     * - 생략(null)·빈 값이면 NONE
     * - 앞뒤 공백 제거, 대소문자 구분 없음 (percent10도 허용)
     * - 목록에 없는 코드면 400
     */
    public static Coupon from(String code) {
        if (code == null || code.isBlank()) {
            return NONE;
        }
        String normalized = code.strip().toUpperCase(Locale.ROOT);
        for (Coupon coupon : values()) {
            if (coupon.name().equals(normalized)) {
                return coupon;
            }
        }
        throw new InvalidCouponException("알 수 없는 쿠폰입니다: " + code.strip());
    }
}