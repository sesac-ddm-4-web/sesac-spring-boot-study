package com.example.hobby_wishlist.presentation;

import com.example.hobby_wishlist.application.WishlistService;
import com.example.hobby_wishlist.dto.wishlist.CreateWishlistItemRequest;
import com.example.hobby_wishlist.dto.wishlist.ImportExternalProductRequest;
import com.example.hobby_wishlist.dto.wishlist.WishlistItemResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/wishlist")
public class WishlistController {
    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @PostMapping
    public ResponseEntity<WishlistItemResponse> createWishlist(@Valid @RequestBody CreateWishlistItemRequest request) {
        WishlistItemResponse response = wishlistService.add(request);
        URI location = URI.create("/wishlist/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public List<WishlistItemResponse> getAllWishlist() {
        return wishlistService.getAll();
    }

    @GetMapping("/{id}")
    public WishlistItemResponse findWishlistById(@PathVariable Long id) {
        return wishlistService.findById(id);
    }

    @GetMapping(params = "budget")
    public List<WishlistItemResponse> recommend(@RequestParam @PositiveOrZero Integer budget) {
        return wishlistService.recommend(budget);
    }

    @PostMapping("/import")
    public ResponseEntity<WishlistItemResponse> importExternalProduct(@Valid @RequestBody ImportExternalProductRequest request) {
        WishlistItemResponse response = wishlistService.importExternal(request.id());
        URI location = URI.create("/wishlist/import/" + response.id());
        return ResponseEntity.created(location).body(response);
    }
}
