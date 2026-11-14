package com.example.personal_project.presentation.dto.shoppingCart;

import jakarta.validation.constraints.NotNull;

public record ShoppingCartRequest(
        @NotNull
        Long collectionItemId,

        @NotNull
        Integer amount) {
}
