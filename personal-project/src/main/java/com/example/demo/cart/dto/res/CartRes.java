package com.example.demo.cart.dto.res;

import com.example.demo.cart.domain.Cart;

import java.util.List;

public record CartRes(
        List<CartItemRes> items,
        long subtotal
) {
    public static CartRes from(Cart cart) {
        return new CartRes(
                cart.getItems().stream().map(CartItemRes::from).toList(),
                cart.subtotal()
        );
    }
}
