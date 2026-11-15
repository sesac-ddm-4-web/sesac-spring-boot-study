package com.example.demo.cart.dto.res;

import com.example.demo.cart.domain.CartItem;

public record CartItemRes(
        Long id, // 장바구니 id
        Long itemId, // 원본 위시리스트 id
        String name,
        int unitPrice,
        Integer stock,
        int quantity,
        long lineTotal
) {
    public static CartItemRes from(CartItem cartItem) {
        return new CartItemRes(
                cartItem.getId(),
                cartItem.getItemId(),
                cartItem.getName(),
                cartItem.getUnitPrice(),
                cartItem.getStock(),
                cartItem.getQuantity(),
                cartItem.lineTotal()
        );
    }
}
