package com.example.sesac_spring_boot_study.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DummyJsonProductList(
        List<DummyJsonProduct> products,
        Integer total,
        Integer skip,
        Integer limit) {
}
