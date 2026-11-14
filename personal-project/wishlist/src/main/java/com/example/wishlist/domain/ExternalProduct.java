package com.example.wishlist.domain;

public record ExternalProduct(
        Long id,
        String title,
        int price,
        String category,
        String thumbnail,
        Integer stock) {
}
