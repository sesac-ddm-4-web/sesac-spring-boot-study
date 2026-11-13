package com.example.wishlist.item.controller;

import com.example.wishlist.item.dto.ItemCreateRequest;
import com.example.wishlist.item.dto.ItemImportRequest;
import com.example.wishlist.item.dto.ItemResponse;
import com.example.wishlist.item.dto.ItemUpdateRequest;
import com.example.wishlist.item.dto.RecommendationResponse;
import com.example.wishlist.item.service.WishlistImportService;
import com.example.wishlist.item.service.WishlistService;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private static final String ITEMS_PATH = "/api/wishlist/items/";

    private final WishlistService service;
    private final WishlistImportService importService;

    public WishlistController(WishlistService service, WishlistImportService importService) {
        this.service = service;
        this.importService = importService;
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ItemResponse> register (@RequestBody ItemCreateRequest request) {
        ItemResponse response = ItemResponse.from(
            service.register(request.name(), request.price())
        );
        return ResponseEntity.created(locationOf(response)).body(response);
    }

    @PostMapping("/items/import")
    public ResponseEntity<ItemResponse> importProduct(@RequestBody ItemImportRequest request) {
        ItemResponse response = ItemResponse.from(
            importService.importProduct(request.validatedExternalId()));
        return ResponseEntity.created(locationOf(response)).body(response);
    }

    @GetMapping("/items")
    public ResponseEntity<List<ItemResponse>> findAll() {
        List<ItemResponse> response = service.findAll().stream()
            .map(ItemResponse::from)
            .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<ItemResponse> findById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ItemResponse.from(service.findById(id)));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<ItemResponse> update(@PathVariable("id") Long id,
        @RequestBody ItemUpdateRequest request) {
        ItemResponse response = ItemResponse.from(
            service.update(id, request.name(), request.price()));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/recommendations")
    public ResponseEntity<RecommendationResponse> recommend(@RequestParam("budget") Integer budget) {
        return ResponseEntity.ok(RecommendationResponse.of(budget, service.recommend(budget)));
    }

    private URI locationOf(ItemResponse item) {
        return URI.create(ITEMS_PATH + item.id());
    }
}
