package com.example.wishlist.product.service;

import com.example.wishlist.product.client.ProductClient;
import com.example.wishlist.product.domain.ExternalProduct;
import com.example.wishlist.product.domain.ExternalProductPage;
import com.example.wishlist.product.dto.ProductQuery;
import org.springframework.stereotype.Service;

@Service
public class ExternalProductService {

    private final ProductClient client;

    public ExternalProductService(ProductClient client) {
        this.client = client;
    }

    public ExternalProductPage findAll(ProductQuery query) {
        return client.findAll(query.limit(), query.skip());
    }

    public ExternalProductPage search(ProductQuery query) {
        return client.search(query.keyword(), query.limit(), query.skip());
    }

    public ExternalProduct findById(long id) {
        return client.findById(id);
    }

}
