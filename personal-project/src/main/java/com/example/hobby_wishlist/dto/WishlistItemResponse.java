package com.example.hobby_wishlist.dto;

import com.example.hobby_wishlist.domain.WishlistItem;

public record WishlistItemResponse(Long id, String name, Integer price) {

    public static WishlistItemResponse from(WishlistItem item){
        return new WishlistItemResponse(item.getId(), item.getName(), item.getPrice());
    }
}
