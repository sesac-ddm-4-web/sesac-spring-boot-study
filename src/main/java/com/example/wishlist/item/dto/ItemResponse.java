package com.example.wishlist.item.dto;

import com.example.wishlist.item.domain.SourceType;
import com.example.wishlist.item.domain.WishlistItem;

public record ItemResponse(
    Long id,
    String name,
    int price,
    SourceType sourceType,
    Long externalId,
    Integer stock,
    boolean soldOut
) {

    public static ItemResponse from(WishlistItem item) {
        return new ItemResponse(
            item.getId(),
            item.getName(),
            item.getPrice(),
            item.getSourceType(),
            item.getExternalId(),
            item.getStock(),
            item.isSoldOut()
        );
    }
}