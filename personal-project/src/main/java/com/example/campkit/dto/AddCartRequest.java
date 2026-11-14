
package com.example.campkit.dto;

import jakarta.validation.constraints.*;

public record AddCartRequest(
        @NotNull @Positive Long itemId,
        @NotNull @Min(1) @Max(5) Integer quantity
) {}
