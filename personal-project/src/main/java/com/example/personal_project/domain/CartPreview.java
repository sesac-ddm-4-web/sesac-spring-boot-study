package com.example.personal_project.domain;

// 장바구니 금액 계산 결과
public record CartPreview(
        long subtotal,
        long discount,
        long discountedSubtotal,
        long shipping,
        long finalAmount,
        Coupon coupon
) {
}
