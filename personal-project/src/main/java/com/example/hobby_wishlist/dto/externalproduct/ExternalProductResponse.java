package com.example.hobby_wishlist.dto.externalproduct;

import com.example.hobby_wishlist.domain.externalproduct.ExternalProduct;

import java.math.BigDecimal;

public record ExternalProductResponse(
        Long externalId,
        String title,
        BigDecimal price,
        String category,
        String thumbnail,
        Integer stock) {
    public static ExternalProductResponse from(ExternalProduct product) {
        return new ExternalProductResponse(product.id(), product.title(), product.price(), product.category(), product.thumbnail(), product.stock());
    }
}
