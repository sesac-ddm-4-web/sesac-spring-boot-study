package com.example.hobby_wishlist.presentation;

import com.example.hobby_wishlist.domain.WishlistItem;
import jakarta.validation.constraints.NotNull;

public record WishlistItemDto(Long id, @NotNull String name, @NotNull Integer price) {
    public WishlistItem toEntity(){
        return WishlistItem.create(id, name, price);
    }

    public static WishlistItemDto create(WishlistItem item){
        return new WishlistItemDto(item.getId(), item.getName(), item.getPrice());
    }
}
