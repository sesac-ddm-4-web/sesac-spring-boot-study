package com.example.demo.externalproduct.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DummyJsonProduct(
        Long id,
        String title,
        BigDecimal price, // 소수 계산이 정확하도록 BigDecimal로
        String category,
        String thumbnail,
        Integer stock
) {
}
