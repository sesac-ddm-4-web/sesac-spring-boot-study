package com.example.wishlist.coupon.domain;

import com.example.wishlist.coupon.exception.InvalidCouponException;

// 쿠폰 하나 적용한 금액 계산 결과
public record PriceBreakdown(
    long subtotal,
    long discount,
    long discountedSubtotal,
    long shipping,
    long finalAmount,
    Coupon coupon
) {

    public static PriceBreakdown calculate(long subtotal, Coupon coupon) {
        if (subtotal < 0) {
            throw new IllegalArgumentException("상품 합계는 음수일 수 없습니다. subtotal=" + subtotal);
        }
        if (subtotal == 0 && coupon != Coupon.NONE) {
            throw new InvalidCouponException("빈 장바구니에는 NONE만 사용할 수 있습니다. (요청 쿠폰: " + coupon + ")");
        }
        if (!coupon.isApplicableTo(subtotal)) {
            throw new InvalidCouponException(String.format(
                "%s 쿠폰은 %s 사용할 수 있습니다. (현재 상품 합계 %,d원)", coupon, coupon.condition(), subtotal));
        }

        long discount = coupon.discountFor(subtotal);
        long discountedSubtotal = subtotal - discount;
        long shipping = ShippingPolicy.feeFor(subtotal, discountedSubtotal);
        long finalAmount = Math.addExact(discountedSubtotal, shipping);

        return new PriceBreakdown(subtotal, discount, discountedSubtotal, shipping, finalAmount, coupon);
    }
}