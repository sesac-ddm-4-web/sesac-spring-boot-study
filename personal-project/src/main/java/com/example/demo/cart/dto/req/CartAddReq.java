package com.example.demo.cart.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartAddReq(
        @NotNull
        @Positive
        Long itemId,

        @NotNull
        @Min(1)
        @Max(5)
        Integer quantity
) {
}
