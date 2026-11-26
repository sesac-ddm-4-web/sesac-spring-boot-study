package com.example.personal_project.presentation.controller;

import com.example.personal_project.application.CartService;
import com.example.personal_project.presentation.dto.cart.AddCartItemRequest;
import com.example.personal_project.presentation.dto.cart.UpdateCartItemRequest;
import com.example.personal_project.presentation.dto.cart.CartItemResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/cart/items")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public List<CartItemResponse> add(@RequestBody AddCartItemRequest request) {
        return cartService.add(request.itemId(), request.quantity()).stream()
                .map(CartItemResponse::from).toList();
    }

    @GetMapping
    public List<CartItemResponse> findAll() {
        return cartService.findAll().stream().map(CartItemResponse::from).toList();
    }

    @PatchMapping("/{cartItemId}")
    public List<CartItemResponse> changeQuantity(
            @PathVariable("cartItemId") Long cartItemId,
            @RequestBody UpdateCartItemRequest request) {
        return cartService.changeQuantity(cartItemId, request.quantity()).stream()
                .map(CartItemResponse::from).toList();
    }

    @DeleteMapping("/{cartItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable("cartItemId") Long cartItemId) {
        cartService.remove(cartItemId);
    }
}
