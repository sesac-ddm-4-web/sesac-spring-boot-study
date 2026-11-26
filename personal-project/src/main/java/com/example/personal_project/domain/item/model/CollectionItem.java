package com.example.personal_project.domain.item.model;

public record CollectionItem(
        Long id,
        String name,
        Long price,
        SourceType sourceType,
        Long externalId,
        Integer stock,
        String category,
        String image
) {
    public CollectionItem withId(Long id) {
        return new CollectionItem(
                id,
                name,
                price,
                sourceType,
                externalId,
                stock,
                category,
                image
        );
    }
}
