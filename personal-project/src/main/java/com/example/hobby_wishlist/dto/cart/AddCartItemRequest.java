package com.example.hobby_wishlist.dto.cart;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(@NotNull Long wishlistItemId, @NotNull @Min(1) @Max(5) Integer quantity) {
}
