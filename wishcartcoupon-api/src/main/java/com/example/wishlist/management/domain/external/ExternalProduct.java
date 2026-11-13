package com.example.wishlist.management.domain.external;

import java.math.BigDecimal;

public record ExternalProduct(long id, String title, BigDecimal price,
                              String category, String thumbnail, int stock) {
}
