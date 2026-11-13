package com.example.wishlist.management.dto.cart;

import com.example.wishlist.management.domain.cart.CartItem;

public record CartItemResponse(long id, long wishId, String name, int price,
                               Integer stock, int quantity, long lineAmount) {
    public static CartItemResponse from(CartItem item) {
        return new CartItemResponse(item.getId(), item.getWishId(), item.getName(),
                item.getPrice(), item.getStock(), item.getQuantity(), item.lineAmount());
    }
}