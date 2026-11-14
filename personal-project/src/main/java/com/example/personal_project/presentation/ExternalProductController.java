package com.example.personal_project.presentation;

import com.example.personal_project.infrastructure.DummyJsonClient;
import com.example.personal_project.infrastructure.DummyJsonProduct;
import com.example.personal_project.infrastructure.DummyJsonProductList;
import org.springframework.web.bind.annotation.*;

// 내 서버로 들어온 외부 상품 조회 요청을 받음
@RestController
@RequestMapping("/external-products")
public class ExternalProductController {
    private final DummyJsonClient client;

    public ExternalProductController(DummyJsonClient client) {
        this.client = client;
    }

    // 상품 한 개 조회
    @GetMapping("/{id}")
    public DummyJsonProduct findById(
            @PathVariable("id") long id) {
        return client.findById(id);
    }

    // 외부 상품 목록 조회
    @GetMapping
    public DummyJsonProductList findAll (
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int skip) {
        return client.findAll(limit, skip);
    }

    // 상품 검색
    @GetMapping("/search")
    public DummyJsonProductList search(
            // 요청의 q 값을 자바 변수 keyword로 받음
            @RequestParam("q") String keyword,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int skip) {
        return client.search(keyword, limit, skip);
    }

}
