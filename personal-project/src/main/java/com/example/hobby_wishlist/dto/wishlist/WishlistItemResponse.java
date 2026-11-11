package com.example.hobby_wishlist.dto.wishlist;

import com.example.hobby_wishlist.domain.wishlist.SourceType;
import com.example.hobby_wishlist.domain.wishlist.WishlistItem;

public record WishlistItemResponse(Long id, SourceType sourceType, Long externalId,
                                   String name, Integer price, Integer stock) {

    public static WishlistItemResponse from(WishlistItem item) {
        return new WishlistItemResponse(item.getId(), item.getSourceType(), item.getExternalId(),
                item.getName(), item.getPrice(), item.getStock());
    }
}
