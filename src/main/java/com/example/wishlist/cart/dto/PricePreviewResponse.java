package com.example.wishlist.cart.dto;

import com.example.wishlist.coupon.domain.PriceBreakdown;

public record PricePreviewResponse(
    long subtotal,
    long discount,
    long discountedSubtotal,
    long shipping,
    long finalAmount,
    String coupon
) {

    public static PricePreviewResponse from(PriceBreakdown breakdown) {
        return new PricePreviewResponse(
            breakdown.subtotal(),
            breakdown.discount(),
            breakdown.discountedSubtotal(),
            breakdown.shipping(),
            breakdown.finalAmount(),
            breakdown.coupon().name()
        );
    }
}
