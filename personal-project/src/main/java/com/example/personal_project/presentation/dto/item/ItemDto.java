package com.example.personal_project.presentation.dto.item;

import com.example.personal_project.domain.item.model.CollectionItem;
import com.example.personal_project.domain.item.model.SourceType;

public record ItemDto(
        Long id,
        String name,
        Long price,
        Integer stock,
        SourceType sourceType,
        Long externalId,
        String category,
        String image
) {
    public static ItemDto from(CollectionItem collectionItem) {
        return new ItemDto(
                collectionItem.id(),
                collectionItem.name(),
                collectionItem.price(),
                collectionItem.stock(),
                collectionItem.sourceType(),
                collectionItem.externalId(),
                collectionItem.category(),
                collectionItem.image()
        );
    }
}
