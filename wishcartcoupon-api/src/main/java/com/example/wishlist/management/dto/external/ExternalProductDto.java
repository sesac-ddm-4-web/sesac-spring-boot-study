package com.example.wishlist.management.dto.external;

import com.example.wishlist.management.domain.external.ExternalProduct;
import com.example.wishlist.management.domain.external.ExternalPriceConverter;

import java.math.BigDecimal;

public record ExternalProductDto(long id, String title, BigDecimal price, int priceWon,
                                 String category, String thumbnail, int stock) {
    public static ExternalProductDto from(ExternalProduct p){
        return new ExternalProductDto(p.id(), p.title(), p.price(),
                ExternalPriceConverter.toWon(p.price()),
                p.category(), p.thumbnail(), p.stock());
    }
}
