package com.example.personal_project.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DummyJsonProductsResponse(
        List<DummyJsonProduct> products,
        Integer total,
        Integer skip,
        Integer limit
) {
}
