package com.example.personal_project.presentation;

import com.example.personal_project.domain.CartItem;
import com.example.personal_project.domain.SourceType;

// 서버가 반환할 장바구니 행 정보
public record CartItemResponse (
        Long id,
        Long itemId,
        String name,
        long unitPrice,
        SourceType sourceType,
        Integer stock,
        int quantity
){
    public static CartItemResponse from (CartItem cartItem) {
        return new CartItemResponse(
                cartItem.getId(),
                cartItem.getItemId(),
                cartItem.getName(),
                cartItem.getUnitPrice(),
                cartItem.getSourceType(),
                cartItem.getStock(),
                cartItem.getQuantity()
        );
    }
}
