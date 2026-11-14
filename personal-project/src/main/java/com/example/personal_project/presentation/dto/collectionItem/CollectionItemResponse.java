package com.example.personal_project.presentation.dto.collectionItem;

import com.example.personal_project.domain.entity.CollectionItem;

public record CollectionItemResponse(Long id, String name, Integer price, Integer stock) {
    public static CollectionItemResponse form(CollectionItem collectionItem){
        return new CollectionItemResponse(
                collectionItem.getId(),
                collectionItem.getName(),
                collectionItem.getPrice(),
                collectionItem.getStock());
    }
}
