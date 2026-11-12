package com.example.sesac_spring_boot_study.domain;

public record CartPreview(
        long subtotal,
        long discount,
        long discountedSubtotal,
        long shipping,
        long finalAmount,
        Coupon coupon) {
}
