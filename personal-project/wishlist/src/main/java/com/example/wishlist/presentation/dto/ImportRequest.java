package com.example.wishlist.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ImportRequest(
        @NotNull(message = "가져올 외부 상품 id(externalId)를 입력해 주세요.")
        @Positive(message = "외부 상품 id는 양수여야 합니다.")
        Long externalId) {
}
