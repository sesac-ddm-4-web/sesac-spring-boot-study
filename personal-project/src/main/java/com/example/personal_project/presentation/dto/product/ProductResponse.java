package com.example.personal_project.presentation.dto.product;

import com.example.personal_project.domain.product.model.ExternalProduct;

import java.math.BigDecimal;

public record ProductResponse(
        Long externalId,
        String title,
        BigDecimal price,
        String category,
        String thumbnail,
        Integer stock
) {
    public static ProductResponse from(ExternalProduct product) {
        return new ProductResponse(
                product.externalId(),
                product.title(),
                product.price(),
                product.category(),
                product.thumbnail(),
                product.stock()
        );
    }
}
