package com.example.wishlistbudget.dto;

import java.util.List;

public record CartResponse(
        List<CartItemResponse> items,
        int itemCount,
        int totalQuantity,
        long subtotal
) {
}