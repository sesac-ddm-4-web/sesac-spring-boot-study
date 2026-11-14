package com.example.personal_project.presentation;

import com.example.personal_project.application.CartService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService =cartService;
    }

    // 목록 조회
    @GetMapping
    public List<CartItemResponse> getItems() {
        return cartService.getItems();
    }

    // 담기 요청
    @PostMapping("/items")
    public CartItemResponse add(
            @Valid @RequestBody AddCartItemRequest request) {
        return cartService.add(request.itemId(), request.quantity());
    }
}
