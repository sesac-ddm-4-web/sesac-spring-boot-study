package com.example.hobby_wishlist.presentation;

import com.example.hobby_wishlist.application.CartService;
import com.example.hobby_wishlist.dto.cart.AddCartItemRequest;
import com.example.hobby_wishlist.dto.cart.CartItemResponse;
import com.example.hobby_wishlist.dto.cart.ChangeCartItemQuantityRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public CartItemResponse addToCart(@Valid @RequestBody AddCartItemRequest request) {
        return cartService.addItem(request);
    }

    @PatchMapping("/{cartItemId}")
    public CartItemResponse changeQuantity(@PathVariable Long cartItemId, @Valid @RequestBody ChangeCartItemQuantityRequest request) {
        return cartService.changeQuantity(cartItemId, request);
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long cartItemId) {
        cartService.removeItem(cartItemId);

        // 204(삭제 완료, 본문 없음)
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<CartItemResponse> getCart() {
        return cartService.getAllItems();
    }
}
