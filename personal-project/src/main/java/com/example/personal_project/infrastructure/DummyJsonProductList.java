package com.example.personal_project.infrastructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

// 목록·검색 응답 DTO. total 등 사용하지 않는 항목은 무시
@JsonIgnoreProperties(ignoreUnknown = true)
public record DummyJsonProductList(
        // 응답 JSON의 products 배열
        List<DummyJsonProduct> products
) {

}
