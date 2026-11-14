package com.example.wishlistbudget.controller;

import com.example.wishlistbudget.domain.CollectionItem;
import com.example.wishlistbudget.dto.CollectionRequest;
import com.example.wishlistbudget.dto.CollectionResponse;
import com.example.wishlistbudget.external.dto.ExternalProductImportRequest;
import com.example.wishlistbudget.service.CollectionService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/collections")
@Validated
public class CollectionController {

    private final CollectionService service;

    public CollectionController(CollectionService service) {
        this.service = service;
    }

    // 아이템 등록
    @PostMapping
    public ResponseEntity<CollectionResponse> addItem(
            @Valid @RequestBody CollectionRequest request
    ) {

        CollectionItem item = service.addItem(
                request.name(),
                request.price()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CollectionResponse.from(item));
    }

    // 전체 아이템 조회
    @GetMapping
    public List<CollectionResponse> getAllItems() {

        List<CollectionItem> items = service.getAllItems();
        List<CollectionResponse> responses = new ArrayList<>();

        for (CollectionItem item : items) {
            responses.add(CollectionResponse.from(item));
        }

        return responses;
    }

    // 예산별 아이템 추천
    @GetMapping("/recommend")
    public List<CollectionResponse> recommend(
            @RequestParam @Min(value = 0, message = "예산은 0 이상이어야 합니다.") int budget
    ) {

        List<CollectionItem> items = service.recommend(budget);
        List<CollectionResponse> responses = new ArrayList<>();

        for (CollectionItem item : items) {
            responses.add(CollectionResponse.from(item));
        }

        return responses;
    }

    // ID로 아이템 조회
    @GetMapping("/{id}")
    public CollectionResponse getItem(
            @PathVariable Long id
    ) {

        CollectionItem item = service.getItem(id);

        return CollectionResponse.from(item);
    }

    // 외부 상품을 위시리스트에 등록
    @PostMapping("/import")
    public ResponseEntity<CollectionResponse> importProduct(
            @Valid @RequestBody ExternalProductImportRequest request
    ) {
        CollectionItem item = service.importProduct(
                request.externalId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CollectionResponse.from(item));
    }
}