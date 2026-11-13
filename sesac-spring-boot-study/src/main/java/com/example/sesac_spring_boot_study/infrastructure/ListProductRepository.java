package com.example.sesac_spring_boot_study.infrastructure;

import com.example.sesac_spring_boot_study.domain.Product;
import com.example.sesac_spring_boot_study.domain.ProductRepository;
import com.example.sesac_spring_boot_study.domain.SourceType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

//@Repository
public class ListProductRepository implements ProductRepository {
    private List<Product> products = new CopyOnWriteArrayList<>();
    private AtomicLong id = new AtomicLong(1L);

    @Override
    public Product add(Product product) {
        product.setId(id.getAndIncrement());
        products.add(product);
        return product;
    }

    @Override
    public Product findById(Long id) {
        return products.stream()
                .filter(product -> product.sameId(id))
                .findFirst()
                .orElseThrow();
    }

    @Override
    public List<Product> findAll() {
        return products;
    }

    @Override
    public boolean existsByExternalId(SourceType type, Long externalId){
        return products.stream()
                .filter(product -> product.getSourceType() == type)
                .filter(product -> externalId.equals(product.getExternalId()))
                .findFirst()
                .isPresent();
    }
}
