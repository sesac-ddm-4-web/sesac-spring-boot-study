package com.example.personal_project.presentation.controller;

import com.example.personal_project.infrastructure.DummyJsonClient;
import com.example.personal_project.presentation.dto.dummyJsonProduct.DummyJsonProductResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExternalItemController {
    private final DummyJsonClient dummyJsonClient;

    public ExternalItemController(DummyJsonClient dummyJsonClient) {
        this.dummyJsonClient = dummyJsonClient;
    }

    @GetMapping("/external-item")
    public DummyJsonProductResponse externalItemList(
            @RequestParam(defaultValue = "10")
            @Max(value = 20, message = "limit은 20이하입니다")
            @Min(value = 1,message = "limit은 1이상입니다")
            Integer limit,
            @RequestParam(defaultValue = "0")
            @Min(value = 0,message = "skip은 0이상입니다")
            Integer skip) {
        return dummyJsonClient.getProducts(limit, skip);
    }

    @GetMapping("/external-item/search")
    public DummyJsonProductResponse externalItemSearch(
            @RequestParam
            @NotBlank(message = "검색어는 비어있을 수 없습니다")
            String q,
            @RequestParam(defaultValue = "10")
            @Max(value = 20, message = "limit은 20이하입니다")
            @Min(value = 1,message = "limit은 1이상입니다")
            Integer limit,
            @RequestParam(defaultValue = "0")
            @Min(value = 0,message = "skip은 0이상입니다")
            Integer skip){
        return dummyJsonClient.searchProducts(q,limit,skip);
    }
}
