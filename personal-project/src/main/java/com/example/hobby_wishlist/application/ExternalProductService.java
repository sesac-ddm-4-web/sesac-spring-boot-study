package com.example.hobby_wishlist.application;

import com.example.hobby_wishlist.domain.externalproduct.ExternalProductProvider;
import com.example.hobby_wishlist.dto.externalproduct.ExternalProductResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExternalProductService {

    private final ExternalProductProvider externalProductProvider;

    public ExternalProductService(ExternalProductProvider externalProductProvider) {
        this.externalProductProvider = externalProductProvider;
    }

    public List<ExternalProductResponse> findAll(Integer limit, Integer skip) {
        return externalProductProvider.findAll(limit, skip)
                .stream()
                .map(ExternalProductResponse::from)
                .toList();
    }

    public List<ExternalProductResponse> search(String query, Integer limit, Integer skip) {
        return externalProductProvider.search(query, limit, skip)
                .stream()
                .map(ExternalProductResponse::from)
                .toList();
    }

    public ExternalProductResponse findById(Long id) {
        var response = externalProductProvider.findById(id);
        return ExternalProductResponse.from(response);
    }
}
