package com.example.wishlist.cart.dto;

import com.example.wishlist.cart.domain.Cart;
import java.util.List;

public record CartResponse(
    List<CartItemResponse> items,
    int kindCount,
    int totalQuantity,
    long subtotal
) {

    public static CartResponse from(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
            .map(CartItemResponse::from)
            .toList();
        return new CartResponse(items, items.size(), cart.totalQuantity(), cart.subtotal());
    }
}