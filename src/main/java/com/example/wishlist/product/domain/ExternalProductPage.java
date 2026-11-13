package com.example.wishlist.product.domain;

import java.util.List;

// 외부 상품 목록/검색 결과 페이지
public record ExternalProductPage(List<ExternalProduct> products, int total, int skip, int limit) {

    public ExternalProductPage {
        products = List.copyOf(products);
    }
}