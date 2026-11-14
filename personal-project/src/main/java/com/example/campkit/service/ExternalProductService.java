
package com.example.campkit.service;

import com.example.campkit.external.*;
import com.example.campkit.exception.ExternalApiException;
import org.springframework.stereotype.Service;

@Service
public class ExternalProductService {

    private final ExternalProductClient client;

    public ExternalProductService(ExternalProductClient client) {
        this.client = client;
    }

    public DummyJsonPage list(int limit, int skip) {
        validatePaging(limit, skip);
        return client.list(limit, skip);
    }

    public DummyJsonPage search(String query, int limit, int skip) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("검색어를 입력해 주세요.");
        }

        validatePaging(limit, skip);
        return client.search(query.trim(), limit, skip);
    }

    public DummyJsonProduct findById(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("상품 ID는 1 이상이어야 합니다.");
        }

        DummyJsonProduct product = client.findById(id);

        if (product == null ||
                product.id() == null ||
                product.title() == null ||
                product.title().isBlank() ||
                product.price() == null ||
                product.price().signum() < 0 ||
                product.stock() == null ||
                product.stock() < 0) {
            throw new ExternalApiException("외부 상품 데이터가 올바르지 않습니다.");
        }

        return product;
    }

    private void validatePaging(int limit, int skip) {
        if (limit < 1 || limit > 20 || skip < 0) {
            throw new IllegalArgumentException(
                    "limit은 1~20, skip은 0 이상이어야 합니다."
            );
        }
    }
}
