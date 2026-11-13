package com.example.wishlist.cart.dto;

import com.example.wishlist.cart.domain.CartItem;

public record CartItemResponse(
    Long id,
    Long wishlistItemId,
    String name,
    long unitPrice,
    int quantity,
    Integer stockLimit,
    long lineTotal
) {

    public static CartItemResponse from(CartItem item) {
        return new CartItemResponse(
            item.getId(),
            item.getWishlistItemId(),
            item.getName(),
            item.getUnitPrice(),
            item.getQuantity(),
            item.getStockLimit(),
            item.lineTotal()
        );
    }
}