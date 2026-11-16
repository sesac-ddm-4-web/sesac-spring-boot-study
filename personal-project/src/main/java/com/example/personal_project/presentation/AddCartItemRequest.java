package com.example.personal_project.presentation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// 장바구니 담기 요청 DTO
// 사용자가 보낸 상품 번호·추가 수량
public record AddCartItemRequest (
        @NotNull
        @Min(1)
        Long itemId,
        @NotNull
        @Min(1)
        @Max(5)
        Integer quantity) {

}
