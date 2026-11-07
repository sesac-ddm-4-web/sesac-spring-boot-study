package com.example.demo.presentation;

import com.example.demo.domain.CollectionItem;

public record CollectionItemDto(
        Long id,
        String name,
        Integer price
) {

    public static CollectionItem toEntity(CollectionItemDto dto){
        return new CollectionItem(dto.id, dto.name, dto.price);
    }

    public static CollectionItemDto toDto(CollectionItem collectionItem){
        return new CollectionItemDto(
                collectionItem.getId(),
                collectionItem.getName(),
                collectionItem.getPrice()
        );
    }
}
