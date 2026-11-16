package com.example.personal_project.presentation;

import com.example.personal_project.domain.CartPreview;
import com.example.personal_project.domain.Coupon;

// 금액 미리보기 응답 DTO
public record CartPreviewResponse(
        long subtotal,
        long discount,
        long discountedSubtotal,
        long shipping,
        long finalAmount,
        Coupon coupon
) {
    public static CartPreviewResponse from(CartPreview preview) {
        return new CartPreviewResponse(
                preview.subtotal(),
                preview.discount(),
                preview.discountedSubtotal(),
                preview.shipping(),
                preview.finalAmount(),
                preview.coupon()
        );
    }
}