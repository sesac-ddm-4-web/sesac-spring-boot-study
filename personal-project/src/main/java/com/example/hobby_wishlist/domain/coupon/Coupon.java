package com.example.hobby_wishlist.domain.coupon;

public enum Coupon {
    NONE {
        @Override
        public Long getDiscountAmount(Long subtotal) {
            return 0L;
        }

        @Override
        boolean isRedeemable(Long subtotal) {
            return true;
        }
    },
    PERCENT10 {
        @Override
        public Long getDiscountAmount(Long subtotal) {
            return Math.min(subtotal * 10 / 100, 20_000L);
        }

        @Override
        boolean isRedeemable(Long subtotal) {
            return subtotal > 0;
        }
    },
    FIXED15000 {
        @Override
        public Long getDiscountAmount(Long subtotal) {
            return Math.min(subtotal, 15000);
        }

        @Override
        boolean isRedeemable(Long subtotal) {
            return subtotal >= 100_000L;
        }
    };

    public abstract Long getDiscountAmount(Long subtotal);

    abstract boolean isRedeemable(Long subtotal);

    public static Coupon getCoupon(String couponCode, Long subtotal) {
        Coupon coupon;
        try {
            coupon = couponCode == null || couponCode.isBlank()
                    ? Coupon.NONE
                    : Coupon.valueOf(couponCode.strip());
        } catch (IllegalArgumentException e) {
            throw new CouponNotFoundException("%s 쿠폰은 존재하지 않습니다.".formatted(couponCode));
        }

        if (!coupon.isRedeemable(subtotal)) {
            throw new CouponNotApplicableException("%s 쿠폰은 적용할 수 없습니다.".formatted(couponCode));
        }

        return coupon;
    }
}
