package com.example.wishlist.product.dto;

import com.example.wishlist.product.domain.ExternalProductPage;
import java.util.List;

// 외부 상품 목록, 검색 응답 DTO
public record ExternalProductPageResponse(
    List<ExternalProductResponse> products,
    int total,
    int skip,
    int limit
) {

    public static ExternalProductPageResponse from(ExternalProductPage page) {
        return new ExternalProductPageResponse(
            page.products().stream().map(ExternalProductResponse::from).toList(),
            page.total(),
            page.skip(),
            page.limit()
        );
    }
}