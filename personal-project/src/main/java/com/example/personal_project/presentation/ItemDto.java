package com.example.personal_project.presentation;

import com.example.personal_project.domain.Item;

public record ItemDto (
        Long id,
        String name,
        Integer price
){
    public static ItemDto from(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getPrice()
        );
    }
}
