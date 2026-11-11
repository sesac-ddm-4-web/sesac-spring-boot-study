package com.example.demo.externalproduct.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DummyJsonProductList(
        List<DummyJsonProduct> products
) {
}
