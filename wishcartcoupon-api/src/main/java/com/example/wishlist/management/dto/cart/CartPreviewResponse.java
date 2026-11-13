package com.example.wishlist.management.dto.cart;

import com.example.wishlist.management.domain.cart.CartPreview;

public record CartPreviewResponse(long subtotal, long discount, long discountedSubtotal,
                                  long shipping, long finalAmount, String coupon) {
    public static CartPreviewResponse from(CartPreview p) {
        return new CartPreviewResponse(p.subtotal(), p.discount(), p.discountedSubtotal(),
                p.shipping(), p.finalAmount(), p.coupon().name());
    }
}