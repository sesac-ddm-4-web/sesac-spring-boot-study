package com.example.sesac_spring_boot_study.application;

import com.example.sesac_spring_boot_study.domain.Product;
import com.example.sesac_spring_boot_study.domain.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimpleProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private SimpleProductService simpleProductService;
    private List<Product> sampleProducts() {
        List<Product> products = new ArrayList<>(); // 수정 가능한 리스트 → 서비스가 정렬하면 바로 드러남
        products.add(new Product(1L, "노트", 12000));
        products.add(new Product(2L, "무료 샘플", 0));
        products.add(new Product(3L, "펜", 5000));
        products.add(new Product(4L, "스티커", 5000));
        products.add(new Product(5L, "게임", 8000));
        return products;
    }

    @Test
    @DisplayName("이름이 공백인 경우")
    void emptyName(){
        boolean thrown = false;
        try {
            simpleProductService.register(" ", 1000);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
    }

    @Test
    @DisplayName("가격이 음수인 경우")
    void minusPrice(){
        boolean thrown = false;
        try {
            simpleProductService.register("공책", -1);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);

    }

    @Test
    @DisplayName("예산이 음수인 경우")
    void minusBudget(){
        boolean thrown = false;
        try {
            simpleProductService.recommend(-1);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);

    }
    @Test
    @DisplayName("정상적인 이름과 가격의 상품 등록")
    void registerProduct() {
        when(productRepository.add(any(Product.class)))
                .thenReturn(new Product(1L, "노트", 12000));

        Product result = simpleProductService.register("노트", 12000);

        assertEquals(1L, result.getId());
        assertEquals("노트", result.getName());
        assertEquals(12000, result.getPrice());
    }

    @Test
    @DisplayName("예산 이하 상품만 가격 오름차순으로 추천")
    void recommendFilterAndSort() {
        when(productRepository.findAll()).thenReturn(sampleProducts());

        List<Product> result = simpleProductService.recommend(8000);

        // 8000 이하: 무료 샘플(0), 펜(5000), 스티커(5000), 게임(8000) → 최대 3개
        assertEquals(3, result.size());
        assertEquals("무료 샘플", result.get(0).getName());
        assertEquals("펜", result.get(1).getName());
        assertEquals("스티커", result.get(2).getName());
    }

}