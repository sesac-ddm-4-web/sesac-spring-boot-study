package com.example.wishlist.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartQuantityRequest(
        @NotNull(message = "수량(quantity)을 입력해 주세요.")
        @Min(value = 1, message = "수량은 1개부터 5개까지 가능합니다.")
        @Max(value = 5, message = "수량은 1개부터 5개까지 가능합니다.")
        Integer quantity) {
}
