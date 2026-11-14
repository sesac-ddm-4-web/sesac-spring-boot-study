package com.example.wishlistbudget.repository;

import com.example.wishlistbudget.domain.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartRepository {

    CartItem save(
            Long collectionItemId,
            String name,
            int price,
            Integer stock,
            int quantity
    );

    Optional<CartItem> findById(Long id);

    Optional<CartItem> findByCollectionItemId(Long collectionItemId);

    List<CartItem> findAll();

    CartItem update(CartItem item);

    boolean deleteById(Long id);
}