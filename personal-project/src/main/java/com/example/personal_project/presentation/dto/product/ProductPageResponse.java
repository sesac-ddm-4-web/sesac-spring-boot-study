package com.example.personal_project.presentation.dto.product;

import com.example.personal_project.domain.product.model.ExternalProductPage;

import java.util.List;

public record ProductPageResponse(
        List<ProductResponse> products,
        int total,
        int skip,
        int limit
) {
    public static ProductPageResponse from(ExternalProductPage page) {
        return new ProductPageResponse(
                page.products().stream()
                        .map(ProductResponse::from)
                        .toList(),
                page.total(),
                page.skip(),
                page.limit()
        );
    }
}
