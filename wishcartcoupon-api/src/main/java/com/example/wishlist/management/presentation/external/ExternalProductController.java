package com.example.wishlist.management.presentation.external;

import com.example.wishlist.management.application.external.ExternalProductService;
import com.example.wishlist.management.dto.external.ExternalProductDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/external-products")
public class ExternalProductController {
    private final ExternalProductService externalProductService;

    public ExternalProductController(ExternalProductService externalProductService) {
        this.externalProductService = externalProductService;
    }

    @GetMapping
    public List<ExternalProductDto> list(
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) int limit,
            @RequestParam(defaultValue = "0") @Min(0) int skip) {
        return externalProductService.list(limit, skip).stream().map(ExternalProductDto::from).toList();
    }

    @GetMapping("/search")
    public List<ExternalProductDto> search(
            @RequestParam(required = false) @NotBlank String q,
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) int limit,
            @RequestParam(defaultValue = "0") @Min(0) int skip) {
        return externalProductService.search(q, limit, skip).stream().map(ExternalProductDto::from).toList();
    }

    @GetMapping("/{id}")
    public ExternalProductDto get(@PathVariable @Min(1) long id) {
        return ExternalProductDto.from(externalProductService.get(id));
    }
}
