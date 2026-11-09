package com.example.sesac_spring_boot_study.application;

//예산 검증: 0 이상 (음수 거부)
//아이템 등록 (검증 통과 후 레포 호출, 등록 결과 반환)
//예산 추천: 예산 이하 필터 → 가격 오름차순(같은 가격은 등록 순서 유지) → 최대 3개 → 없으면 빈 목록

import com.example.sesac_spring_boot_study.domain.Product;
import com.example.sesac_spring_boot_study.domain.ProductRepository;
import com.example.sesac_spring_boot_study.infrastructure.ListProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SimpleProductService {
    private ProductRepository productRepository;
    private int MAX_RECOMMENDATIONS = 3;

    @Autowired
    SimpleProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public String nameCheck(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 비어 있을 수 없습니다.");
        }
        return name.trim();
    }

    public int priceCheck(int price) {
        if (price < 0) {
            throw new IllegalArgumentException("가격은 0 이상의 정수여야 합니다.");
        }
        return price;
    }

    public int budgetCheck(Integer budget) {
        if (budget == null || budget < 0) {
            throw new IllegalArgumentException("예산은 0 이상이어야 합니다.");
        }
        return budget;
    }


    public Product register(String name, int price) {
        Product product = new Product(null, nameCheck(name), priceCheck(price));
        return productRepository.add(product);
    }

    public Product findById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public List<Product> recommend(Integer budget) {
        int limit = budgetCheck(budget);

        List<Product> candidates = new ArrayList<>();
        for (Product product : productRepository.findAll()) {
            if (product.getPrice() <= limit) {
                candidates.add(product);
            }
        }
        candidates.sort(Comparator.comparingInt(Product::getPrice));
        int end = Math.min(MAX_RECOMMENDATIONS, candidates.size());
        return new ArrayList<>(candidates.subList(0, end));
    }

}
