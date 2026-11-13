package com.example.wishlist.cart.dto;

import com.example.wishlist.common.exception.InvalidRequestException;

public record CartAddRequest(Long wishlistItemId, Integer quantity) {

    public void validate() {
        if (wishlistItemId == null) {
            throw new InvalidRequestException("wishlistItemId는 필수입니다.");
        }
        if (quantity == null) {
            throw new InvalidRequestException("quantity는 필수입니다.");
        }
    }
}