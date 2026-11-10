package com.example.hobby_wishlist.dto;

import jakarta.validation.constraints.NotNull;

public record ImportExternalProductRequest(@NotNull Long id) {
}
