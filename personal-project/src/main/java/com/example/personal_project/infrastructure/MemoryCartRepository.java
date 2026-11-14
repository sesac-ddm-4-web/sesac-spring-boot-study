package com.example.personal_project.infrastructure;

import com.example.personal_project.domain.Cart;
import com.example.personal_project.domain.CartRepository;
import org.springframework.stereotype.Repository;

@Repository
public class MemoryCartRepository implements CartRepository {
    private final Cart cart = new Cart();

    @Override
    public Cart getCart() {
        return cart;
    }
}
