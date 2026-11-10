package com.example.hobby_wishlist.infrastructure.external.dummyjson.model;


import com.example.hobby_wishlist.domain.ExternalProduct;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DummyJsonProductPage(
        List<ExternalProduct> products,
        Integer total,
        Integer skip,
        Integer limit
) {

}
