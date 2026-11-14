package com.example.wishlist.presentation.dto;

import com.example.wishlist.domain.PaymentPreview;

public record PaymentPreviewResponse(
        long subtotal,
        long discount,
        long discountedSubtotal,
        long shipping,
        long finalAmount,
        String coupon) {

    public static PaymentPreviewResponse from(PaymentPreview preview) {
        return new PaymentPreviewResponse(
                preview.subtotal(),
                preview.discount(),
                preview.discountedSubtotal(),
                preview.shipping(),
                preview.finalAmount(),
                preview.coupon().name());
    }
}
