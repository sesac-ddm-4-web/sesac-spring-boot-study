package com.example.wishlist.presentation.dto;

import com.example.wishlist.domain.ExternalProduct;

public record ExternalProductResponse(
        Long id,
        String title,
        int price,
        String category,
        String thumbnail,
        Integer stock) {

    public static ExternalProductResponse from(ExternalProduct product) {
        return new ExternalProductResponse(
                product.id(),
                product.title(),
                product.price(),
                product.category(),
                product.thumbnail(),
                product.stock());
    }
}
