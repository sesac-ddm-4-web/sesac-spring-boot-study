package com.example.wishlist.product.client;

import com.example.wishlist.product.domain.ExternalProduct;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

// DummyJSON 상품의 JSON 응답 담는 DTO
@JsonIgnoreProperties(ignoreUnknown = true)
public record DummyJsonProduct(
    Long id,
    String title,
    BigDecimal price,
    String category,
    String thumbnail,
    Integer stock
) {

    ExternalProduct toExternalProduct() {
        return new ExternalProduct(id, title, price, category, thumbnail, stock);
    }
}