package com.example.wishlistbudget.external.controller;

import com.example.wishlistbudget.external.dto.DummyJsonProduct;
import com.example.wishlistbudget.external.dto.DummyJsonProductList;
import com.example.wishlistbudget.external.dto.ExternalProductListRequest;
import com.example.wishlistbudget.external.dto.ExternalProductSearchRequest;
import com.example.wishlistbudget.external.service.ExternalProductService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/external/products")
public class ExternalProductController {

    private final ExternalProductService externalProductService;

    public ExternalProductController(ExternalProductService externalProductService) {
        this.externalProductService = externalProductService;
    }

    // 외부 상품 목록 조회
    @GetMapping
    public DummyJsonProductList getProducts(
            @Valid @ModelAttribute ExternalProductListRequest request)
    {
        return externalProductService.getProducts(
                request.limit(),
                request.skip()
        );
    }

    // 외부 상품 검색
    @GetMapping("/search")
    public DummyJsonProductList searchProducts(
            @Valid @ModelAttribute ExternalProductSearchRequest request)
    {
        return externalProductService.searchProducts(request.q());
    }

    // 외부 상품 단건 조회
    @GetMapping("/{id}")
    public DummyJsonProduct getProduct(
            @PathVariable Long id)
    {
        return externalProductService.getProduct(id);
    }

}
