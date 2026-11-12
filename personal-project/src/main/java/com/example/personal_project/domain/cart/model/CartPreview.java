package com.example.personal_project.domain.cart.model;

public record CartPreview(long subtotal, long discount, long discountedSubtotal,
                          long shipping, long finalAmount, Coupon coupon) {
}
