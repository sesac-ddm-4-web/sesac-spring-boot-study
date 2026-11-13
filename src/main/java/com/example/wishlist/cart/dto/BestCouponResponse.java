package com.example.wishlist.cart.dto;

import com.example.wishlist.coupon.domain.CouponComparison;
import java.util.List;

public record BestCouponResponse(PricePreviewResponse best, List<PricePreviewResponse> candidates) {

    public static BestCouponResponse from(CouponComparison comparison) {
        return new BestCouponResponse(
            PricePreviewResponse.from(comparison.best()),
            comparison.candidates().stream().map(PricePreviewResponse::from).toList()
        );
    }
}