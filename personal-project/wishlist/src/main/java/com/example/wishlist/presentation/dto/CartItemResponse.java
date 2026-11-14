package com.example.wishlist.presentation.dto;

import com.example.wishlist.domain.CartItem;

public record CartItemResponse(
        Long id,
        Long wishItemId,
        String name,
        long unitPrice,
        Integer stock,
        int quantity,
        long lineTotal) {

    public static CartItemResponse from(CartItem item) {
        return new CartItemResponse(
                item.getId(),
                item.getWishItemId(),
                item.getName(),
                item.getUnitPrice(),
                item.getStock(),
                item.getQuantity(),
                item.getLineTotal());
    }
}
