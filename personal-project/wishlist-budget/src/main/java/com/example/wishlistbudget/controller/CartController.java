package com.example.wishlistbudget.controller;

import com.example.wishlistbudget.domain.CartItem;
import com.example.wishlistbudget.domain.CouponType;

import com.example.wishlistbudget.dto.CartAddRequest;
import com.example.wishlistbudget.dto.CartItemResponse;
import com.example.wishlistbudget.dto.CartPreviewResponse;
import com.example.wishlistbudget.dto.CartQuantityRequest;
import com.example.wishlistbudget.dto.CartResponse;

import com.example.wishlistbudget.service.CartService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // 장바구니 상품 추가
    @PostMapping("/items")
    public ResponseEntity<CartItemResponse> addItem(
            @Valid @RequestBody CartAddRequest request
    ) {

        CartItem item = cartService.addItem(
                request.collectionItemId(),
                request.quantity()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CartItemResponse.from(item));
    }

    // 장바구니 전체 조회
    @GetMapping
    public CartResponse getCart() {

        return cartService.getCart();
    }

    // 장바구니 상품 수량 변경
    @PutMapping("/items/{id}")
    public CartItemResponse updateQuantity(
            @PathVariable Long id,
            @Valid @RequestBody CartQuantityRequest request
    ) {

        CartItem item = cartService.updateQuantity(
                id,
                request.quantity()
        );

        return CartItemResponse.from(item);
    }

    // 장바구니 상품 삭제
    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long id
    ) {

        cartService.deleteItem(id);

        return ResponseEntity.noContent().build();
    }

    // 결제 예상 금액 미리보기
    @GetMapping("/preview")
    public CartPreviewResponse preview(
            @RequestParam(defaultValue = "NONE") CouponType coupon
    ) {

        return cartService.preview(coupon);
    }
}