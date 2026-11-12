package com.example.personal_project.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DummyJsonProduct(
        Long id,
        String title,
        BigDecimal price,
        String category,
        String thumbnail,
        Integer stock
) {
}
