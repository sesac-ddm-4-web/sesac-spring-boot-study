package com.example.sesac_spring_boot_study.domain;

import java.util.List;

public interface CartRepository {
    //Cart getCart();
    List<CartItem> findAll();
    CartItem add(CartItem cartItem);
    CartItem update(CartItem cartItem);
    void delete(Long rowId);
}
