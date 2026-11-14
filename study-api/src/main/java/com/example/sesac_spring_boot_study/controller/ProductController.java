package com.example.sesac_spring_boot_study.controller;

import com.example.sesac_spring_boot_study.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/greet")
    public String hello(@RequestParam(name="name",defaultValue = "방문자")String name){
        String line = productService.greet(name);
        return line;
    }
}
