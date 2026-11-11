package com.example.demo.cart.dto.res;

import com.example.demo.cart.domain.Coupon;
import com.example.demo.cart.domain.PricePreview;

public record PricePreviewRes(
        long subtotal,
        long discount,
        long discountedSubtotal,
        long shipping,
        long finalAmount,
        Coupon coupon
) {
    public static PricePreviewRes from(PricePreview preview) {
        return new PricePreviewRes(
                preview.subtotal(),
                preview.discount(),
                preview.discountedSubtotal(),
                preview.shipping(),
                preview.finalAmount(),
                preview.coupon()
        );
    }
}
