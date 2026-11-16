package com.example.personal_project.infrastructure;

import com.example.personal_project.domain.Cart;
import com.example.personal_project.domain.CartRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile({"default", "test"})
public class MemoryCartRepository implements CartRepository {
    private Cart cart = new Cart();

    @Override
    public Cart getCart() {
        return cart;
    }

    @Override
    public void save(Cart cart) {
        this.cart = cart;
    }
}
