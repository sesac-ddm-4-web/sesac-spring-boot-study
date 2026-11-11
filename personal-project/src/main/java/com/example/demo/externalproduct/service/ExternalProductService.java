package com.example.demo.externalproduct.service;

import com.example.demo.externalproduct.client.ExternalProductClient;
import com.example.demo.externalproduct.client.dto.DummyJsonProduct;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExternalProductService {

    private final ExternalProductClient externalProductClient;

    public ExternalProductService(ExternalProductClient externalProductClient) {
        this.externalProductClient = externalProductClient;
    }

    public List<DummyJsonProduct> findAll(int limit, int skip) {
        return externalProductClient.findAll(limit, skip);
    }

    public List<DummyJsonProduct> search(String q, int limit, int skip) {
        return externalProductClient.search(q.strip(), limit, skip);
    }

    public DummyJsonProduct findById(long id) {
        return externalProductClient.findById(id);
    }
}
