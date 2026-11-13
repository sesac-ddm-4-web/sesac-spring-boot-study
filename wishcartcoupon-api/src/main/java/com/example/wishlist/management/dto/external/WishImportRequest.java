package com.example.wishlist.management.dto.external;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record WishImportRequest(@NotNull @Min(1) long externalId) {
}
