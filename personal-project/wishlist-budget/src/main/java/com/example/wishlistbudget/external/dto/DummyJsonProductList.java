package com.example.wishlistbudget.external.dto;

import java.util.List;

//DummyJSON 서버가 JSON으로 보내준 상품 정보를 내 Java 객체로 변환해서 담기 위한 DTO
public record DummyJsonProductList(
        List<DummyJsonProduct> products,
        int total,
        int skip,
        int limit
)
{}
