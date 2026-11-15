package com.example.demo.cart.domain;

import com.example.demo.global.exception.BadRequestException;

// 금액 미리보기
// 상품 합계, 쿠폰 할인, 할인 후 금액, 배송비, 최종 금액
public record PricePreview(
        long subtotal,
        long discount,
        long discountedSubtotal,
        long shipping,
        long finalAmount,
        Coupon coupon
) {
    private static final long FREE_SHIPPING_MIN = 100_000;
    private static final long SHIPPING_FEE = 5_000;

    public static PricePreview of(long subtotal, Coupon coupon) {
        if (!coupon.isApplicable(subtotal)) {
            throw new BadRequestException(coupon + " 쿠폰을 사용할 수 없습니다.");
        }

        long discount = coupon.discount(subtotal);
        long discountedSubtotal = subtotal - discount;

        long shipping;
        if (subtotal == 0 || discountedSubtotal >= FREE_SHIPPING_MIN) {
            shipping = 0;
        } else {
            shipping = SHIPPING_FEE;
        }

        return new PricePreview(
                subtotal, discount, discountedSubtotal, shipping, discountedSubtotal + shipping, coupon);
    }
}
