package com.example.wishlist.support;

import com.example.wishlist.product.client.ProductClient;
import com.example.wishlist.product.domain.ExternalProduct;
import com.example.wishlist.product.domain.ExternalProductPage;
import com.example.wishlist.product.exception.ExternalApiException;
import com.example.wishlist.product.exception.ExternalProductNotFoundException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class FakeProductClient implements ProductClient {

    private final Map<Long, ExternalProduct> products = new LinkedHashMap<>();
    private boolean failing = false;
    private int findByIdCalls = 0;

    public FakeProductClient add(long id, String title, String price, int stock) {
        products.put(id, new ExternalProduct(id, title, new BigDecimal(price), "books", "https://example.com/" + id + ".png", stock));
        return this;
    }

    /** 외부 서버 장애 흉내 (502) */
    public void failAll() {
        this.failing = true;
    }

    public int findByIdCalls() {
        return findByIdCalls;
    }

    @Override
    public ExternalProductPage findAll(int limit, int skip) {
        checkFailing();
        List<ExternalProduct> all = new ArrayList<>(products.values());
        List<ExternalProduct> page = all.stream().skip(skip).limit(limit).toList();
        return new ExternalProductPage(page, all.size(), skip, limit);
    }

    @Override
    public ExternalProductPage search(String keyword, int limit, int skip) {
        checkFailing();
        List<ExternalProduct> matched = products.values().stream()
            .filter(p -> p.title().toLowerCase().contains(keyword.toLowerCase()))
            .toList();
        return new ExternalProductPage(matched.stream().skip(skip).limit(limit).toList(), matched.size(), skip, limit);
    }

    @Override
    public ExternalProduct findById(long id) {
        findByIdCalls++;
        checkFailing();
        ExternalProduct product = products.get(id);
        if (product == null) {
            throw new ExternalProductNotFoundException(id);
        }
        return product;
    }

    private void checkFailing() {
        if (failing) {
            throw new ExternalApiException("외부 상품 정보를 가져오지 못했습니다. (테스트용 장애)");
        }
    }
}
