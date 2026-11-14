package com.example.wishlist.presentation;

import com.example.wishlist.application.ExternalProductService;
import com.example.wishlist.application.WishService;
import com.example.wishlist.domain.CollectionItem;
import com.example.wishlist.presentation.dto.ImportRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class WishController {
    private final WishService service;
    private final ExternalProductService externalProductService;

    public WishController(WishService service, ExternalProductService externalProductService) {
        this.service = service;
        this.externalProductService = externalProductService;
    }

    @PostMapping("/wish/items")
    public CollectionItem register(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "price", required = false) Integer price) {
        return service.register(name, price);
    }

    @PostMapping("/wish/items/import")
    public CollectionItem importExternal(@Valid @RequestBody ImportRequest request) {
        return externalProductService.importToWishlist(request.externalId());
    }

    @GetMapping("/wish/items")
    public List<CollectionItem> findAll() {
        return service.findAll();
    }

    @GetMapping("/wish/items/{id}")
    public CollectionItem findById(@PathVariable("id") Long id) {
        return service.findById(id);
    }

    @PutMapping("/wish/items/{id}")
    public CollectionItem update(
            @PathVariable("id") Long id,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "price", required = false) Integer price) {
        return service.update(id, name, price);
    }

    @DeleteMapping("/wish/items/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/wish/recommendations")
    public List<CollectionItem> recommend(
            @RequestParam(name = "budget", required = false) Integer budget) {
        return service.recommend(budget);
    }
}
