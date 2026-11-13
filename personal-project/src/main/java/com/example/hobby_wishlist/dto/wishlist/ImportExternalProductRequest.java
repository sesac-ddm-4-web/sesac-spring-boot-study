package com.example.hobby_wishlist.dto.wishlist;

import jakarta.validation.constraints.NotNull;

public record ImportExternalProductRequest(@NotNull Long externalId) {
}
