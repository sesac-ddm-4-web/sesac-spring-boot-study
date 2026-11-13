package com.example.wishlist.management.dto.cart;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartAddRequest(@NotNull @Min(1) Long wishId,
                             @NotNull @Min(1) @Max(5) Integer quantity) {}
