package com.example.demo.externalproduct.controller;

import com.example.demo.externalproduct.client.dto.DummyJsonProduct;
import com.example.demo.externalproduct.service.ExternalProductService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
public class ExternalProductController {

    private final ExternalProductService externalProductService;

    public ExternalProductController(ExternalProductService externalProductService) {
        this.externalProductService = externalProductService;
    }

    @GetMapping("/external-products")
    public List<DummyJsonProduct> findAll(
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) int limit,
            @RequestParam(defaultValue = "0") @Min(0) int skip) {
        return externalProductService.findAll(limit, skip);
    }

    @GetMapping("/external-products/search")
    public List<DummyJsonProduct> search(
            @RequestParam @NotBlank String q,
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) int limit,
            @RequestParam(defaultValue = "0") @Min(0) int skip) {
        return externalProductService.search(q, limit, skip);
    }

    @GetMapping("/external-products/{id}")
    public DummyJsonProduct findById(@PathVariable("id") long id) {
        return externalProductService.findById(id);
    }
}
