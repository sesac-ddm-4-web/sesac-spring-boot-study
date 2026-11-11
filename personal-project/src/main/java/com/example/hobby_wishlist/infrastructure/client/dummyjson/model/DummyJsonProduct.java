package com.example.hobby_wishlist.infrastructure.client.dummyjson.model;

import com.example.hobby_wishlist.domain.externalproduct.ExternalProduct;

import java.math.BigDecimal;

public record DummyJsonProduct(
        Long id,
        String title,
        BigDecimal price,
        String category,
        String thumbnail,
        Integer stock
) {
    public ExternalProduct toExternalProduct(){
        return new ExternalProduct(id, title, price, category, thumbnail, stock);
    }
}
