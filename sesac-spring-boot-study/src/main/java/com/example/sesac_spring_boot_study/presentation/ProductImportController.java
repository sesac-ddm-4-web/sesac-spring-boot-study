package com.example.sesac_spring_boot_study.presentation;

import com.example.sesac_spring_boot_study.application.ExternalProductService;
import com.example.sesac_spring_boot_study.domain.Product;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductImportController {
    private ExternalProductService externalProductService;

    public ProductImportController (ExternalProductService externalProductService){
        this.externalProductService=externalProductService;
    }

    @PostMapping("/products/import/{externalId}")
    public Product importProduct(@PathVariable Long externalId) {
        return externalProductService.importProduct(externalId);
    }

}
