package com.example.hobby_wishlist.dto.cart;

import com.example.hobby_wishlist.domain.cart.Cart;

import java.util.List;

public record CartResponse(List<CartItemResponse> items, Long subtotal) {
    public static CartResponse from(Cart cart) {
        return new CartResponse(
                cart.getItems().stream()
                        .map(CartItemResponse::from)
                        .toList(),
                cart.getSubTotal()
        );
    }
}
