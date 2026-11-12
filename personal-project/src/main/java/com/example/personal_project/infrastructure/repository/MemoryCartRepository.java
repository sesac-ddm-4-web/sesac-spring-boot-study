package com.example.personal_project.infrastructure.repository;

import com.example.personal_project.domain.cart.model.Cart;
import com.example.personal_project.domain.cart.repository.CartRepository;
import org.springframework.stereotype.Repository;

@Repository
public class MemoryCartRepository implements CartRepository {

    private Cart cart = new Cart();

    @Override
    public Cart find() {
        return cart.copy();
    }

    @Override
    public void save(Cart cart) {
        this.cart = cart.copy();
    }
}
