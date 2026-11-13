package com.example.wishlist.cart.repository;

import com.example.wishlist.cart.domain.Cart;
import org.springframework.stereotype.Repository;

//@Repository
public class CartRepositoryImpl implements CartRepository {

    private Cart cart = new Cart();

    @Override
    public synchronized Cart load() {
        return cart.copy();
    }

    @Override
    public synchronized void save(Cart cart) {
        this.cart = cart.copy();
    }
}
