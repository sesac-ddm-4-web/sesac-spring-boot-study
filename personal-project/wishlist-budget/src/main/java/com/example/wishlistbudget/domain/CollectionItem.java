package com.example.wishlistbudget.domain;

// 위시리스트에 저장되는 상품 정보
public record CollectionItem(
        Long id,             // 내 위시리스트 상품 ID
        String name,         // 상품 이름
        int price,           // 상품 가격
        SourceType sourceType, // 상품 등록 출처
        Long externalId,     // DummyJSON 상품 ID
        Integer stock        // 외부 상품 재고
) {
}