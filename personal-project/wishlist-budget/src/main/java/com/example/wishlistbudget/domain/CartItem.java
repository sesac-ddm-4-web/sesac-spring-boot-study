package com.example.wishlistbudget.domain;

public record CartItem(
        Long id,
        Long collectionItemId,
        String name,
        int price,
        Integer stock,
        int quantity
) {
}