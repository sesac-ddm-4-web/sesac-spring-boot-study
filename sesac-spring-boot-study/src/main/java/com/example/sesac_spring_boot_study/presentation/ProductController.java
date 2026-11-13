package com.example.sesac_spring_boot_study.presentation;

import com.example.sesac_spring_boot_study.application.SimpleProductService;
import com.example.sesac_spring_boot_study.domain.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
public class ProductController {

    private SimpleProductService simpleProductService;

    @Autowired
    ProductController(SimpleProductService simpleProductService) {
        this.simpleProductService = simpleProductService;
    }

    public record CreateProductRequest(String name, int price) {}

    @PostMapping(value = "/products")
    public Product createProduct(@RequestBody CreateProductRequest request) {
        return simpleProductService.register(request.name(), request.price());
    }

    @GetMapping(value = "/products")
    public List<Product> findAll() {
        return simpleProductService.findAll();
    }

    @GetMapping("/products/{id}")
    public Product getProduct(@PathVariable Long id) {
        return simpleProductService.findById(id);
    }

    @GetMapping("/recommend/{budget}")
    public List<Product> recommend(@PathVariable Integer budget) {
        return simpleProductService.recommend(budget);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException e) {
        return e.getMessage();
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoSuchElementException e) {
        return e.getMessage();
    }

}
