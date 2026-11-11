package com.example.hobby_wishlist.infrastructure.client.dummyjson.model;


import com.example.hobby_wishlist.domain.externalproduct.ExternalProduct;
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
