package com.example.wishlist.product.client;

import com.example.wishlist.product.domain.ExternalProduct;
import com.example.wishlist.product.domain.ExternalProductPage;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

// DummyJSON 목록, 검색 응답을 담는 DTO
@JsonIgnoreProperties(ignoreUnknown = true)
public record DummyJsonProductPage(
    List<DummyJsonProduct> products,
    Integer total,
    Integer skip,
    Integer limit) {

    ExternalProductPage toExternalProductPage() {
        List<ExternalProduct> converted = products == null
            ? List.of()
            : products.stream().map(DummyJsonProduct::toExternalProduct).toList();
        return new ExternalProductPage(
            converted,
            total == null ? converted.size() : total,
            skip == null ? 0 : skip,
            limit == null ? converted.size() : limit
        );
    }
}