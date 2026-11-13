package com.example.wishlist.management.domain.cart;

import java.util.List;

public record CouponRecommendation(CouponType recommended, List<CartPreview> candidates) {}