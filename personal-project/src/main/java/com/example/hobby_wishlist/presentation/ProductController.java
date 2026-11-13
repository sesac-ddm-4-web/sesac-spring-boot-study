package com.example.hobby_wishlist.presentation;

import com.example.hobby_wishlist.application.ExternalProductService;
import com.example.hobby_wishlist.dto.externalproduct.ExternalProductResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ExternalProductService externalProductService;

    public ProductController(ExternalProductService externalProductService) {
        this.externalProductService = externalProductService;
    }

    @GetMapping
    public List<ExternalProductResponse> getProducts(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) Integer limit,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer skip) {
        if (q == null) {
            return externalProductService.findAll(limit, skip);
        }
        if (q.isBlank()) {
            throw new IllegalArgumentException("검색어를 입력해 주세요.");
        }
        return externalProductService.search(q, limit, skip);
    }

    @GetMapping("/{id}")
    public ExternalProductResponse findById(@PathVariable Long id) {
        return externalProductService.findById(id);
    }
}
