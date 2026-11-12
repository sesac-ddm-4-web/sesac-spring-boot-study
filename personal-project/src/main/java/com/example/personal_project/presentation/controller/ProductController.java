package com.example.personal_project.presentation.controller;

import com.example.personal_project.application.ProductService;
import com.example.personal_project.presentation.dto.product.ProductPageResponse;
import com.example.personal_project.presentation.dto.product.ProductResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ProductPageResponse findAll(
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            @RequestParam(name = "skip", defaultValue = "0") int skip
    ) {
        return ProductPageResponse.from(
                productService.findAll(limit, skip)
        );
    }

    @GetMapping("/search")
    public ProductPageResponse search(
            @RequestParam(name = "q") String query,
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            @RequestParam(name = "skip", defaultValue =  "0") int skip
    ) {
        return ProductPageResponse.from(
                productService.search(query, limit, skip)
        );
    }

    @GetMapping("/{externId}")
    public ProductResponse findById(
            @PathVariable("externId") Long externId
    ) {
        return ProductResponse.from(
                productService.findById(externId)
        );
    }

}
