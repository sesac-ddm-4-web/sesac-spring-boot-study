package com.example.wishlist.management.infrastructure.cart;

import com.example.wishlist.management.domain.cart.Cart;
import com.example.wishlist.management.domain.cart.CartRepository;
import org.springframework.stereotype.Repository;

// 앱이 떠 있는 동안 유지되는 하나의 장바구니
@Repository
public class InMemoryCartRepository implements CartRepository {
    private final Cart cart = new Cart();

    @Override
    public Cart get(){
        return cart;
    }
}
