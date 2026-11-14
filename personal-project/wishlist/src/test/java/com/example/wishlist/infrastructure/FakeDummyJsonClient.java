package com.example.wishlist.infrastructure;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FakeDummyJsonClient extends DummyJsonClient {
    private final Map<Long, DummyJsonProduct> products = new LinkedHashMap<>();

    public int lastLimit;
    public int lastSkip;
    public boolean down;

    public void put(long id, String title, String price, int stock) {
        products.put(id, new DummyJsonProduct(id, title, new BigDecimal(price), "test", "thumb.png", stock));
    }

    @Override
    public DummyJsonProduct findById(long id) {
        checkDown();
        DummyJsonProduct product = products.get(id);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "외부 상품을 찾을 수 없습니다");
        }
        return product;
    }

    @Override
    public List<DummyJsonProduct> findAll(int limit, int skip) {
        checkDown();
        lastLimit = limit;
        lastSkip = skip;
        return new ArrayList<>(products.values());
    }

    @Override
    public List<DummyJsonProduct> search(String q, int limit, int skip) {
        checkDown();
        lastLimit = limit;
        lastSkip = skip;
        return products.values().stream()
                .filter(product -> product.title().contains(q))
                .toList();
    }

    private void checkDown() {
        if (down) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "외부 상품 정보를 가져오지 못했습니다");
        }
    }
}
