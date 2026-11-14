package com.example.wishlist.presentation;

import com.example.wishlist.application.ExternalProductService;
import com.example.wishlist.presentation.dto.ExternalProductResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ExternalProductController {
    private final ExternalProductService service;

    public ExternalProductController(ExternalProductService service) {
        this.service = service;
    }

    @GetMapping("/external-products")
    public List<ExternalProductResponse> findAll(
            @RequestParam(name = "limit", defaultValue = "10")
            @Min(value = 1, message = "limit은 1부터 20 사이여야 합니다.")
            @Max(value = 20, message = "limit은 1부터 20 사이여야 합니다.") int limit,
            @RequestParam(name = "skip", defaultValue = "0")
            @Min(value = 0, message = "skip은 0 이상이어야 합니다.") int skip) {
        return service.findAll(limit, skip).stream()
                .map(ExternalProductResponse::from)
                .toList();
    }

    @GetMapping("/external-products/search")
    public List<ExternalProductResponse> search(
            @RequestParam(name = "q", required = false)
            @NotBlank(message = "검색어(q)를 입력해 주세요.") String q,
            @RequestParam(name = "limit", defaultValue = "10")
            @Min(value = 1, message = "limit은 1부터 20 사이여야 합니다.")
            @Max(value = 20, message = "limit은 1부터 20 사이여야 합니다.") int limit,
            @RequestParam(name = "skip", defaultValue = "0")
            @Min(value = 0, message = "skip은 0 이상이어야 합니다.") int skip) {
        return service.search(q, limit, skip).stream()
                .map(ExternalProductResponse::from)
                .toList();
    }

    @GetMapping("/external-products/{id}")
    public ExternalProductResponse findById(
            @PathVariable("id") @Positive(message = "상품 id는 양수여야 합니다.") long id) {
        return ExternalProductResponse.from(service.findById(id));
    }
}
