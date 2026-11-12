package com.example.personal_project.domain.item.model;

// TODO: 가격을 long으로 확장할 때 DTO, 추천 예산, 저장소와 테스트를 함께 변경한다.
// TODO: 외부 상품 이름·가격 수정 금지 규칙을 도메인에 둔다.
public record CollectionItem(
        Long id,
        String name,
        Long price,
        SourceType sourceType,
        Long externalId,
        Integer stock
) {
}
