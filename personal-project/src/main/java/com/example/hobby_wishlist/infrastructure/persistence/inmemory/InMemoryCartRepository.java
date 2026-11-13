package com.example.hobby_wishlist.infrastructure.persistence.inmemory;

import com.example.hobby_wishlist.domain.cart.Cart;
import com.example.hobby_wishlist.domain.cart.CartRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.concurrent.atomic.AtomicLong;

@Repository
@Profile("test")
public class InMemoryCartRepository implements CartRepository {
    private Cart cart = new Cart();
    private final AtomicLong sequence = new AtomicLong(0L);

    @Override
    public Cart getCart() {
        return cart;
    }

    @Override
    public Cart saveChanges(Cart cart) {
        cart.assignIds(sequence::getAndIncrement);
        this.cart = cart;
        return cart;
    }
}
