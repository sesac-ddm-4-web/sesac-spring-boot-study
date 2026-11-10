package com.example.hobby_wishlist.dto;

import com.example.hobby_wishlist.domain.WishlistItem;
import jakarta.validation.constraints.NotNull;

public record CreateWishlistItemRequest(@NotNull String name, @NotNull Integer price) {

    public WishlistItem toEntity() {
        return WishlistItem.fromManual(null, name, price);
    }
}
