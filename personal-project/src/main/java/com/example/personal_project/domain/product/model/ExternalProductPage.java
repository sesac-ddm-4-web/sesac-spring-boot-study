package com.example.personal_project.domain.product.model;

import java.util.List;

public record ExternalProductPage(
        List<ExternalProduct> products,
        int total,
        int skip,
        int limit
){
    public ExternalProductPage {
        products = List.copyOf(products);
    }
}
