package com.example.wishlistbudget.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

// DummyJSON이 보내는 JSON에 DTO에 없는 필드가 포함되어 있어도 무시하도록 하는 설정
@JsonIgnoreProperties(ignoreUnknown = true)
// DummyJSON 서버가 JSON으로 보내준 상품 정보를 내 Java 객체로 변환해서 담기 위한 DTO
public record DummyJsonProduct(
        Long id,
        String title,
        BigDecimal price,
        String category,
        String thumbnail,
        Integer stock
)
{}
