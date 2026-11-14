package com.example.personal_project.presentation;

import com.example.personal_project.application.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    // 삭제 요청
    @DeleteMapping("/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) //204
    public void remove(@PathVariable("id") Long id) {
        cartService.remove(id);
    }

    // 수량 변경 요청
    @PatchMapping("/items/{id}")
    public CartItemResponse changeQuantity(
            @PathVariable("id") Long id,
            @Valid @RequestBody ChangeQuantityRequest request) {
        return cartService.changeQuantity(id, request.quantity());
    }
}
