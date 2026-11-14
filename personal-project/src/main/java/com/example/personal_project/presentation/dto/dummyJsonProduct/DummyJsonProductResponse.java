package com.example.personal_project.presentation.dto.dummyJsonProduct;

import java.util.List;

public record DummyJsonProductResponse(
        List<DummyJsonProduct> products,
        int total,
        int skip,
        int limit
) {
}
