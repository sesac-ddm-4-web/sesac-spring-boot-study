package com.example.demo.cart.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartQuantityReq(
        @NotNull
        @Min(1)
        @Max(5)
        Integer quantity
) {
}
