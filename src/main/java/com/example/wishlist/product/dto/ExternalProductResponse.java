package com.example.wishlist.product.dto;

import com.example.wishlist.product.domain.ExternalProduct;
import java.math.BigDecimal;

// 외부 상품 응답 DTO
public record ExternalProductResponse(
    Long id,
    String title,
    BigDecimal price,
    int priceWon,
    String category,
    String thumbnail,
    int stock,
    boolean soldOut
) {

    public static ExternalProductResponse from(ExternalProduct product) {
        return new ExternalProductResponse(
            product.id(),
            product.title(),
            product.price(),
            product.priceInWon(),
            product.category(),
            product.thumbnail(),
            product.stock(),
            product.isSoldOut()
        );
    }
}