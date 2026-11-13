package com.example.wishlist.cart.dto;

import com.example.wishlist.common.exception.InvalidRequestException;

public record CartQuantityRequest(Integer quantity) {

    public void validate() {
        if (quantity == null) {
            throw new InvalidRequestException("quantity는 필수입니다.");
        }
    }
}