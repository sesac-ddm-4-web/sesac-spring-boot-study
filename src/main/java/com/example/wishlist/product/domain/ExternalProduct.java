package com.example.wishlist.product.domain;

import com.example.wishlist.product.exception.ExternalApiException;
import java.math.BigDecimal;

// 외부 상품
public record ExternalProduct (
    Long id,
    String title,
    BigDecimal price,
    String category,
    String thumbnail,
    Integer stock
) {

    public ExternalProduct {
        if (id == null || title == null || title.isBlank()
            || price == null || price.signum() < 0
            || stock == null || stock < 0) {
            throw new ExternalApiException("외부 상품 데이터가 올바르지 않습니다. id=" + id);
        }
        title = title.strip();
    }

    public int priceInWon() {
        return PriceConverter.toWon(price);
    }

    public boolean isSoldOut() {
        return stock == 0;
    }
}
