package com.example.hobby_wishlist.dto.cart;

import com.example.hobby_wishlist.domain.cart.Cart;

import java.util.List;

public record CartPreviewResponse(List<CartItemResponse> items, Long total, String coupon, Long couponDiscount, Long shippingFee) {
    public static CartPreviewResponse from(Cart cart, Long total, String coupon, Long couponDiscount, Long shippingFee) {
        return new CartPreviewResponse(
                cart.getItems().stream()
                        .map(CartItemResponse::from)
                        .toList(),
                total, coupon, couponDiscount, shippingFee
        );
    }
}
