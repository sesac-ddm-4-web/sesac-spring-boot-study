package com.example.demo.support;

import com.example.demo.externalproduct.client.ExternalProductClient;
import com.example.demo.externalproduct.client.dto.DummyJsonProduct;
import com.example.demo.global.exception.EntityNotFoundException;
import com.example.demo.global.exception.ExternalApiException;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FakeExternalProductClient implements ExternalProductClient {

    private final Map<Long, DummyJsonProduct> products = new LinkedHashMap<>();
    private boolean failing = false;
    private int findByIdCount = 0;

    public static DummyJsonProduct product(long id, String title, String price, int stock) {
        return new DummyJsonProduct(id, title, new BigDecimal(price), "test", "https://example.com/" + id + ".png", stock);
    }

    public void add(DummyJsonProduct product) {
        products.put(product.id(), product);
    }

    public void fail() {
        failing = true;
    }

    public int findByIdCount() {
        return findByIdCount;
    }

    @Override
    public List<DummyJsonProduct> findAll(int limit, int skip) {
        checkFailing();
        return products.values().stream()
                .sorted(Comparator.comparing(DummyJsonProduct::id))
                .skip(skip)
                .limit(limit)
                .toList();
    }

    @Override
    public List<DummyJsonProduct> search(String q, int limit, int skip) {
        checkFailing();
        return products.values().stream()
                .filter(product -> product.title().toLowerCase().contains(q.toLowerCase()))
                .skip(skip)
                .limit(limit)
                .toList();
    }

    @Override
    public DummyJsonProduct findById(long id) {
        checkFailing();
        findByIdCount++;
        DummyJsonProduct product = products.get(id);
        if (product == null) {
            throw new EntityNotFoundException("외부 상품을 찾지 못했습니다.");
        }
        return product;
    }

    private void checkFailing() {
        if (failing) {
            throw new ExternalApiException("외부 상품 API 호출에 실패했습니다.", new RuntimeException("timeout"));
        }
    }
}
