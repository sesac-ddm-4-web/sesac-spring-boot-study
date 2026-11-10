package com.example.hobby_wishlist.infrastructure.external.dummyjson.model;

import com.example.hobby_wishlist.domain.ExternalProduct;

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
