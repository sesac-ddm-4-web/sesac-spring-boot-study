package com.example.demo.cart.repository;

import com.example.demo.cart.domain.Cart;
import org.springframework.stereotype.Repository;

@Repository
public class MemoryCartRepository implements CartRepository {

    private final Cart cart = new Cart();

    @Override
    public Cart find() {
        return cart;
    }
}
