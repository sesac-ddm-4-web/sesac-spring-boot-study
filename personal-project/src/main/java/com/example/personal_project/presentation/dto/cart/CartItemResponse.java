package com.example.personal_project.presentation.dto.cart;

import com.example.personal_project.domain.cart.model.CartItem;

public record CartItemResponse(
        Long cartItemId,
        Long itemId,
        String name,
        long unitPrice,
        int quantity
) {
    public static CartItemResponse from(CartItem item) {
        return new CartItemResponse(
                item.cartItemId(),
                item.itemId(),
                item.name(),
                item.unitPrice(),
                item.quantity()
        );
    }
}
