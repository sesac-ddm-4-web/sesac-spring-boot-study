package com.example.wishlistbudget.dto;

import com.example.wishlistbudget.domain.CouponType;

public record CartPreviewResponse(
        long subtotal,
        CouponType coupon,
        long discount,
        long discountedSubtotal,
        long shippingFee,
        long finalTotal
) {
}