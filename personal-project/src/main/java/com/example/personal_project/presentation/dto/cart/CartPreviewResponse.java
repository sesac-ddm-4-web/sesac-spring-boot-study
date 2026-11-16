package com.example.personal_project.presentation.dto.cart;

import com.example.personal_project.domain.cart.model.CartPreview;
import com.example.personal_project.domain.cart.model.Coupon;

public record CartPreviewResponse(long subtotal, long discount, long discountedSubtotal,
                                  long shipping, long finalAmount, Coupon coupon) {
    public static CartPreviewResponse from(CartPreview preview) {
        return new CartPreviewResponse(preview.subtotal(), preview.discount(),
                preview.discountedSubtotal(), preview.shipping(), preview.finalAmount(), preview.coupon());
    }
}
