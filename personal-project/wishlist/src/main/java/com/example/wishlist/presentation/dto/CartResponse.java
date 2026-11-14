package com.example.wishlist.presentation.dto;

import com.example.wishlist.domain.Cart;

import java.util.List;

public record CartResponse(
        List<CartItemResponse> items,
        int totalQuantity,
        long subtotal) {

    public static CartResponse from(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(CartItemResponse::from)
                .toList();
        return new CartResponse(items, cart.getTotalQuantity(), cart.getSubtotal());
    }
}
