package com.example.hobby_wishlist.presentation;

import com.example.hobby_wishlist.application.ExternalProductService;
import com.example.hobby_wishlist.dto.ExternalProductResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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

    @GetMapping(params = {"limit", "skip"})
    public List<ExternalProductResponse> getProducts(
            @RequestParam(defaultValue = "10") @Min(0) @Max(20) Integer limit,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer skip) {
        return externalProductService.findAll(limit, skip);
    }

    @GetMapping(params = {"query", "limit", "skip"})
    public List<ExternalProductResponse> getProductsByQuery(
            @RequestParam @NotBlank String query,
            @RequestParam(defaultValue = "10") @Min(0) @Max(20) Integer limit,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer skip) {
        return externalProductService.search(query, limit, skip);
    }

    @GetMapping("/{id}")
    public ExternalProductResponse findById(@PathVariable Long id) {
        return externalProductService.findById(id);
    }
}
