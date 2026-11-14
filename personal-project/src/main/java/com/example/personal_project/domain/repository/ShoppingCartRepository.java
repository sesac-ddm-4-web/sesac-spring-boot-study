package com.example.personal_project.domain.repository;

import com.example.personal_project.domain.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartRepository {
    ShoppingCart add(ShoppingCart shoppingCart);
    Boolean existByCollectionItemId(Long collectionItemId);
    ShoppingCart findByCollectionItemId(Long collectionItemId);
    ShoppingCart update(ShoppingCart shoppingCart);
    List<ShoppingCart> findAll();
    void delete(Long collectionItemId);
}
