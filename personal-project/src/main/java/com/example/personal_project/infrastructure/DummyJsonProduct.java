package com.example.personal_project.infrastructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

// 상품 한 개의 외부 응답 DTO. 선언하지 않은 JSON 항목은 무시
@JsonIgnoreProperties(ignoreUnknown = true)
public record DummyJsonProduct(
        Long id,
        String title,
        BigDecimal price,
        String category,
        String thumbnail,
        Integer stock) {

}
