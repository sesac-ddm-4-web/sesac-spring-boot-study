package com.example.wishlistbudget.external.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;


public record ExternalProductListRequest (

    @Min(1) @Max(20)
    Integer limit,

    @Min(0)
    Integer skip
)

{
    public ExternalProductListRequest {
        if (limit == null) limit = 10;
        if (skip == null) skip = 0;
    }
}
