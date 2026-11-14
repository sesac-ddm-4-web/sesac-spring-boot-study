package com.example.wishlist.domain;

public record PaymentPreview(
        long subtotal,
        long discount,
        long discountedSubtotal,
        long shipping,
        long finalAmount,
        Coupon coupon) {

    public static final long FREE_SHIPPING_MIN = 100_000;
    public static final long SHIPPING_FEE = 5_000;

    public static PaymentPreview of(long subtotal, Coupon coupon) {
        long discount = coupon.discount(subtotal);
        long discountedSubtotal = subtotal - discount;
        long shipping = calculateShipping(subtotal, discountedSubtotal);
        long finalAmount = discountedSubtotal + shipping;
        return new PaymentPreview(subtotal, discount, discountedSubtotal, shipping, finalAmount, coupon);
    }

    private static long calculateShipping(long subtotal, long discountedSubtotal) {
        if (subtotal == 0) {
            return 0;
        }
        if (discountedSubtotal >= FREE_SHIPPING_MIN) {
            return 0;
        }
        return SHIPPING_FEE;
    }
}
