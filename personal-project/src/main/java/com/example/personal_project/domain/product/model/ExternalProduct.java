package com.example.personal_project.domain.product.model;

import java.math.BigDecimal;

public record ExternalProduct(
        Long externalId,
        String title,
        BigDecimal price,
        String category,
        String thumbnail,
        Integer stock
) {
}
