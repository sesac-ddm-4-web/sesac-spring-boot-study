package com.example.hobby_wishlist.presentation;

import com.example.hobby_wishlist.application.WishlistService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishlist")
public class WishlistController {
    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WishlistItemDto createWishlist(@Valid @RequestBody WishlistItemDto itemDto) {
        return wishlistService.add(itemDto);
    }

    @GetMapping
    public List<WishlistItemDto> getAllWishlist() {
        return wishlistService.getAll();
    }

    @GetMapping("/{id}")
    public WishlistItemDto findWishlistById(@PathVariable Long id) {
        return wishlistService.findById(id);
    }

    @GetMapping(params = "budget")
    public List<WishlistItemDto> recommend(@RequestParam @PositiveOrZero Integer budget) {
        return wishlistService.recommend(budget);
    }
}
