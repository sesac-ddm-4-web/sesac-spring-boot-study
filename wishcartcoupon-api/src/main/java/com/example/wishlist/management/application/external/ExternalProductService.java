package com.example.wishlist.management.application.external;

import com.example.wishlist.management.domain.external.ExternalProduct;
import com.example.wishlist.management.domain.external.ExternalProductClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExternalProductService {
    private final ExternalProductClient client;

    public ExternalProductService(ExternalProductClient client){
        this.client = client;
    }

    public List<ExternalProduct> list(int limit, int skip) {
        return client.list(limit, skip);
    }
    public List<ExternalProduct> search(String q, int limit, int skip) {
        return client.search(q, limit, skip);
    }
    public ExternalProduct get(long id) {
        return client.get(id);
    }
}
