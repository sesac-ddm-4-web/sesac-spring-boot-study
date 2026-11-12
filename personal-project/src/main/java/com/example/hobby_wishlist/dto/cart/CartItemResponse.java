package com.example.hobby_wishlist.dto.cart;

import com.example.hobby_wishlist.domain.cart.CartItem;

public record CartItemResponse(Long cartItemId, Long wishlistItemId, String name, Integer price, Integer stock, Integer quantity) {
    public static CartItemResponse from(CartItem cartItem){
        return new CartItemResponse(cartItem.getId(), cartItem.getWishlistId(), cartItem.getName(), cartItem.getPrice(), cartItem.getStock(), cartItem.getQuantity());
    }
}
