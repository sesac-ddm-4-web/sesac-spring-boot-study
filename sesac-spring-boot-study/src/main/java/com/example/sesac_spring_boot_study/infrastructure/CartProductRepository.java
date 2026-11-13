package com.example.sesac_spring_boot_study.infrastructure;

import com.example.sesac_spring_boot_study.domain.Cart;

public class CartProductRepository {
    public Cart getCart() {
        Cart cart = new Cart();
        return cart;
    }
}
