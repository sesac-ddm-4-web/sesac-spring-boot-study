package com.example.wishlistbudget.external.service;

import com.example.wishlistbudget.external.client.DummyJsonClient;
import com.example.wishlistbudget.external.dto.DummyJsonProduct;
import com.example.wishlistbudget.external.dto.DummyJsonProductList;
import org.springframework.stereotype.Service;

@Service
public class ExternalProductService {

    private final DummyJsonClient dummyJsonClient;

    public ExternalProductService(DummyJsonClient dummyJsonClient) {
        this.dummyJsonClient = dummyJsonClient;
    }

    // 외부 상품 단건 조회
    public DummyJsonProduct getProduct(Long id) {
        return dummyJsonClient.getProduct(id);
    }

    // 외부 상품 목록 조회
    public DummyJsonProductList getProducts(int limit, int skip) {
        return dummyJsonClient.getProducts(limit, skip);
    }

    // 외부 상품 검색
    public DummyJsonProductList searchProducts(String q) {
        return dummyJsonClient.searchProducts(q);
    }
}
