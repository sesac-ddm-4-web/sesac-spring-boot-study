package com.example.personal_project.presentation.dto.cart;

public record AddCartItemRequest(
        Long itemId,
        Integer quantity
) {
}
