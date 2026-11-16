package com.example.personal_project.application;

import com.example.personal_project.application.port.ProductClient;
import com.example.personal_project.domain.product.model.ExternalProduct;
import com.example.personal_project.domain.product.model.ExternalProductPage;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductClient productClient;

    public ProductService(ProductClient productClient) {
        this.productClient = productClient;
    }

    public ExternalProductPage findAll(int limit, int skip) {
        validatePage(limit, skip);
        return productClient.findAll(limit, skip);
    }

    public ExternalProductPage search(String query, int limit, int skip) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("검색어를 입력해야 합니다.");
        }

        validatePage(limit, skip);
        return productClient.search(query.trim(), limit, skip);
    }

    public ExternalProduct findById(Long externId) {
        if (externId == null || externId <= 0) {
            throw new IllegalArgumentException("외부 상품 id는 양수여야 합니다.");
        }

        return productClient.findById(externId);
    }

    private void validatePage(int limit, int skip) {
        if (limit < 1 || limit > 20) {
            throw new IllegalArgumentException("limit은 1~20 이어야 합니다.");
        }
        if (skip < 0) {
            throw new IllegalArgumentException("skip은 0 이상이어야 합니다.");
        }
    }
}
