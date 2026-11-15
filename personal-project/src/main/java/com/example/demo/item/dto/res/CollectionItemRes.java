package com.example.demo.item.dto.res;

import com.example.demo.item.domain.CollectionItem;
import com.example.demo.item.domain.SourceType;

public record CollectionItemRes(
        Long id,
        String name,
        Integer price,
        SourceType sourceType,
        Long externalId,
        Integer stock
) {
    public static CollectionItemRes from(CollectionItem item) {
        return new CollectionItemRes(
                item.getId(),
                item.getName(),
                item.getPrice(),
                item.getSourceType(),
                item.getExternalId(),
                item.getStock()
        );
    }
}
