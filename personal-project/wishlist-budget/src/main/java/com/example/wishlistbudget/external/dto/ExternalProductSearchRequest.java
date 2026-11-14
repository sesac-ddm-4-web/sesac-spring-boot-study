package com.example.wishlistbudget.external.dto;

import jakarta.validation.constraints.NotBlank;

public record ExternalProductSearchRequest(

        @NotBlank
        String q
)
{}
