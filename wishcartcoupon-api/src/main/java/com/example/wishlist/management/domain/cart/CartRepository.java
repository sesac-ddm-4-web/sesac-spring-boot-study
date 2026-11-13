package com.example.wishlist.management.domain.cart;

// 단일 장바구니
public interface CartRepository {
    Cart get();
    default void save(Cart cart){}
}