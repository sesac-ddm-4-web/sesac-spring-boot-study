package com.example.wishlistbudget.dto;

import com.example.wishlistbudget.domain.CollectionItem;

public record CollectionResponse(
        Long id,
        String name,
        int price
)
{
    // 정적 팩토리 메서드를 사용해서 도메인 객체 -> 응답 DTO 변환
    public static CollectionResponse from(CollectionItem item) {
        return new CollectionResponse(
                item.id(),
                item.name(),
                item.price()
        );
    }
}
