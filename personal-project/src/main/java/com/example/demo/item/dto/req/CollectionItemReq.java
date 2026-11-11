package com.example.demo.item.dto.req;

import com.example.demo.item.domain.CollectionItem;

public record CollectionItemReq(
        String name,
        Integer price
) {
    public CollectionItem toEntity(Long id) {
        return new CollectionItem(id, name, price);
    }
}
