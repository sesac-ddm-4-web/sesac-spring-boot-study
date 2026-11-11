package com.example.demo.item.dto.req;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ExternalImportReq(
        @NotNull
        @Positive
        Long externalId
) {
}
