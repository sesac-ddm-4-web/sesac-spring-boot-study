package com.example.personal_project.presentation.dto.collectionItem;

import jakarta.validation.constraints.NotNull;

public record CollectionItemRequest(
        @NotNull
        String name,

        @NotNull
        Integer price) {
}
