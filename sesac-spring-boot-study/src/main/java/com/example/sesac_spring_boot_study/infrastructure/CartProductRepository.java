package com.example.sesac_spring_boot_study.infrastructure;

import com.example.sesac_spring_boot_study.domain.Cart;
import com.example.sesac_spring_boot_study.domain.CartRepository;
import org.springframework.stereotype.Repository;

@Repository
public class CartProductRepository implements CartRepository {
    private Cart cart = new Cart();

    @Override
    public Cart getCart() {
        return cart;
    }
}
