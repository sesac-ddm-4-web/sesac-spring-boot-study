package com.example.wishlist.management.domain.cart;

public enum CouponType {
    NONE, PERCENT10, FIXED15000;

    // 10프로 할인 최대 할인값
    private static final long PERCENT10_MAX_DISCOUNT = 20_000;
    // 10만원 이상일 때 15,000원 할인
    private static final long FIXED_DISCOUNT = 15_000;
    // 10만원 이상
    private static final long FIXED_MIN_SUBTOTAL = 100_000;

    // 문자열 -> 쿠폰, 모르는 값이면 400이 되도록 예외 던지기
    public static CouponType from(String value){
        if(value == null) return NONE;
        try {
            return valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("사용할 수 없는 쿠폰입니다. coupon=" + value);
        }
    }

    /// 여기서 사용할 수 있는 쿠폰인가, 사용자가 가지고 있는 쿠폰을 검사
    public boolean isUsable(long subtotal){
        return this != FIXED15000 || subtotal >=  FIXED_MIN_SUBTOTAL;
    }

    // 할인액
    public long discount(long subtotal){
        return switch (this){
            case NONE -> 0;
            case PERCENT10 -> Math.min(subtotal / 10, PERCENT10_MAX_DISCOUNT);
            case FIXED15000 -> FIXED_DISCOUNT;
        };
    }
}
