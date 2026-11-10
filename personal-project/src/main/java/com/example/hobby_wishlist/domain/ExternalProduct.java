package com.example.hobby_wishlist.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.math.RoundingMode;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalProduct(
        Long id,
        String title,
        BigDecimal price,
        String category,
        String thumbnail,
        Integer stock
) {
    private static final long CONVERSION_UNIT = 1000;
    public Integer getConvertedPrice(){
        return price.multiply(BigDecimal.valueOf(CONVERSION_UNIT))
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();
    }
}