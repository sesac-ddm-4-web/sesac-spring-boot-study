package com.example.sesac_spring_boot_study.domain;

import java.util.List;

public interface ProductRepository {
    Product add(Product product);

    Product findById(Long id);

    List<Product> findAll();
}
