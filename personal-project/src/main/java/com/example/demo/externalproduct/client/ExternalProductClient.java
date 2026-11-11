package com.example.demo.externalproduct.client;

import com.example.demo.externalproduct.client.dto.DummyJsonProduct;

import java.util.List;

// service는 이 인터페이스에만 의존하고 실제로 어떤 API를 부르는지는 모름
public interface ExternalProductClient {

    List<DummyJsonProduct> findAll(int limit, int skip);

    List<DummyJsonProduct> search(String q, int limit, int skip);

    DummyJsonProduct findById(long id);
}
