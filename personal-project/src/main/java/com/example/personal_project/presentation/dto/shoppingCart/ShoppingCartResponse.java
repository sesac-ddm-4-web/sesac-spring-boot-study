package com.example.personal_project.presentation.dto.shoppingCart;

import com.example.personal_project.domain.entity.ShoppingCart;

public record ShoppingCartResponse(
        Long collectionItemId,
        String name,
        Integer unitPrice,
        Integer stock,
        Integer amount) {
    public static ShoppingCartResponse form(ShoppingCart shoppingCart){
        return new ShoppingCartResponse(
                shoppingCart.getCollectionItemId(),
                shoppingCart.getName(),
                shoppingCart.getUnitPrice(),
                shoppingCart.getStock(),
                shoppingCart.getAmount());
    }
}
