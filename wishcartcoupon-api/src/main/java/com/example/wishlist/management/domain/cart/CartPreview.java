package com.example.wishlist.management.domain.cart;

public record CartPreview(long subtotal, long discount, long discountedSubtotal,
                          long shipping, long finalAmount, CouponType coupon) {
}
