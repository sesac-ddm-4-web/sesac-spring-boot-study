
package com.example.campkit.dto;

import jakarta.validation.constraints.*;

public record UpdateQuantityRequest(
        @NotNull @Min(1) @Max(5) Integer quantity
) {}
