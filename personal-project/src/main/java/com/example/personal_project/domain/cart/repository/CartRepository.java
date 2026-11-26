package com.example.personal_project.domain.cart.repository;

import com.example.personal_project.domain.cart.model.Cart;

public interface CartRepository {

    Cart find();
    void save(Cart cart);
}
