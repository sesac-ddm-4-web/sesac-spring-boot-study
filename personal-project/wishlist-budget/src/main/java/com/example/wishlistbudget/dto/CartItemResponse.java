package com.example.wishlistbudget.dto;

import com.example.wishlistbudget.domain.CartItem;

public record CartItemResponse(
        Long id,
        Long collectionItemId,
        String name,
        int price,
        Integer stock,
        int quantity,
        long lineTotal
) {

    public static CartItemResponse from(CartItem item) {

        long lineTotal = (long) item.price() * item.quantity();

        return new CartItemResponse(
                item.id(),
                item.collectionItemId(),
                item.name(),
                item.price(),
                item.stock(),
                item.quantity(),
                lineTotal
        );
    }
}