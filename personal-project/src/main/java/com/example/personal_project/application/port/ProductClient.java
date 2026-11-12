package com.example.personal_project.application.port;

import com.example.personal_project.domain.product.model.ExternalProduct;
import com.example.personal_project.domain.product.model.ExternalProductPage;

public interface ProductClient {
    ExternalProductPage findAll(int limit, int skip);
    ExternalProductPage search(String query, int limit, int skip);
    ExternalProduct findById(Long externalId);
}