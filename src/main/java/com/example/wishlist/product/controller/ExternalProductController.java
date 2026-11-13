package com.example.wishlist.product.controller;

import com.example.wishlist.common.exception.InvalidRequestException;
import com.example.wishlist.product.dto.ExternalProductPageResponse;
import com.example.wishlist.product.dto.ExternalProductResponse;
import com.example.wishlist.product.dto.ProductQuery;
import com.example.wishlist.product.service.ExternalProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/external-products")
public class ExternalProductController {

    private final ExternalProductService service;

    public ExternalProductController(ExternalProductService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ExternalProductPageResponse> findAll(
        @RequestParam(name = "limit", required = false) Integer limit,
        @RequestParam(name = "skip", required = false) Integer skip) {
        ExternalProductPageResponse response = ExternalProductPageResponse .from(
            service.findAll(ProductQuery.forList(limit, skip))
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<ExternalProductPageResponse> search(
        @RequestParam(name = "q", required = false) String q,
        @RequestParam(name = "limit", required = false) Integer limit,
        @RequestParam(name = "skip", required = false) Integer skip) {
        ExternalProductPageResponse response = ExternalProductPageResponse.from(
            service.search(ProductQuery.forSearch(q, limit, skip))
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExternalProductResponse> findById(@PathVariable("id") long id) {
        if (id <= 0) {
            throw new InvalidRequestException("상품 id는 1 이상이어야 합니다. id=" + id);
        }
        ExternalProductResponse response = ExternalProductResponse.from(service.findById(id));
        return ResponseEntity.ok(response);
    }


}
