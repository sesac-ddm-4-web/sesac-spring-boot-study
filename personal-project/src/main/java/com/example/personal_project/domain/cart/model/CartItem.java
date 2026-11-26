package com.example.personal_project.domain.cart.model;

import com.example.personal_project.domain.item.model.SourceType;
import com.example.personal_project.global.exception.CartConflictException;

public record CartItem(
        Long cartItemId,
        Long itemId,
        String name,
        long unitPrice,
        SourceType sourceType,
        Integer stock,
        int quantity
) {
    public CartItem {
        if (quantity < 1 || quantity > 5) {
            throw new CartConflictException("상품 수량은 1~5개여야 합니다.");
        }
        if (sourceType == SourceType.DUMMY_JSON) {
            if (stock == null || quantity > stock) {
                throw new CartConflictException("상품 재고가 부족합니다.");
            }
        }
    }

    public CartItem withQuantity(int quantity) {
        return new CartItem(
                cartItemId,
                itemId,
                name,
                unitPrice,
                sourceType,
                stock,
                quantity
        );
    }
}
