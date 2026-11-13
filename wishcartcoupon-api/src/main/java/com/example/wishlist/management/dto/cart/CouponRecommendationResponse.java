package com.example.wishlist.management.dto.cart;

import com.example.wishlist.management.domain.cart.CartPreview;
import com.example.wishlist.management.domain.cart.CouponRecommendation;

import java.util.List;

public record CouponRecommendationResponse(String recommendedCoupon, List<CartPreviewResponse> candidates) {
    public static CouponRecommendationResponse from(CouponRecommendation recommendation){
        return new CouponRecommendationResponse(
                recommendation.recommended().name(),
                recommendation.candidates().stream().map(CartPreviewResponse::from).toList());
    }
}
