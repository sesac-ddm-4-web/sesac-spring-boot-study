package com.example.wishlist.cart.repository;

import com.example.wishlist.cart.domain.Cart;

public interface CartRepository {

    Cart load();

    void save(Cart cart);

}
