package com.example.wishlist.domain;

import java.util.List;

public interface CartItemRepository {

    CartItem add(CartItem cartItem);

    CartItem findById(Long id);

    CartItem findByWishItemId(Long wishItemId);

    List<CartItem> findAll();

    CartItem update(CartItem cartItem);

    boolean delete(Long id);
}
