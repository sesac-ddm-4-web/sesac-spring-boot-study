package com.example.hobby_wishlist.domain;

import java.util.List;

public interface ExternalProductProvider {
    ExternalProduct findById(Long id);
    List<ExternalProduct> findAll(Integer limit, Integer skip);
    List<ExternalProduct> search(String query, Integer limit, Integer skip);
}
