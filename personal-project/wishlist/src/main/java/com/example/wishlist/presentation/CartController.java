package com.example.wishlist.presentation;

import com.example.wishlist.application.CartService;
import com.example.wishlist.presentation.dto.CartAddRequest;
import com.example.wishlist.presentation.dto.CartItemResponse;
import com.example.wishlist.presentation.dto.CartQuantityRequest;
import com.example.wishlist.presentation.dto.CartResponse;
import com.example.wishlist.presentation.dto.PaymentPreviewResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CartController {
    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @GetMapping("/cart")
    public CartResponse findCart() {
        return CartResponse.from(service.findCart());
    }

    @PostMapping("/cart/items")
    public CartItemResponse add(@Valid @RequestBody CartAddRequest request) {
        return CartItemResponse.from(service.add(request.wishItemId(), request.quantity()));
    }

    @PatchMapping("/cart/items/{cartItemId}")
    public CartItemResponse changeQuantity(
            @PathVariable("cartItemId") Long cartItemId,
            @Valid @RequestBody CartQuantityRequest request) {
        return CartItemResponse.from(service.changeQuantity(cartItemId, request.quantity()));
    }

    @DeleteMapping("/cart/items/{cartItemId}")
    public ResponseEntity<Void> remove(@PathVariable("cartItemId") Long cartItemId) {
        service.remove(cartItemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cart/preview")
    public PaymentPreviewResponse preview(
            @RequestParam(name = "couponCode", required = false) String couponCode) {
        return PaymentPreviewResponse.from(service.preview(couponCode));
    }
}
