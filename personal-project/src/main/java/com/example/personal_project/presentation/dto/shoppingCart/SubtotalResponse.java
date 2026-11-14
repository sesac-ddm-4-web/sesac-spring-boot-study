package com.example.personal_project.presentation.dto.shoppingCart;

public record SubtotalResponse(
        Long subtotal,
        Long discount,
        Long discountedSubtotal,
        Integer shipping,
        Long finalPrice,
        String coupon
) {
}
