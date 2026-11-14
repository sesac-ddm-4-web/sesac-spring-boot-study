package com.example.wishlistbudget.external.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// 외부 상품을 위시리스트에 등록하기 위한 요청 DTO
public record ExternalProductImportRequest(

        @NotNull @Min(1)
        Long externalId
)
{}
