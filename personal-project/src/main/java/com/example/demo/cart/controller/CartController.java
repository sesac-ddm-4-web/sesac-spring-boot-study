package com.example.demo.cart.controller;

import com.example.demo.cart.dto.req.CartAddReq;
import com.example.demo.cart.dto.req.CartQuantityReq;
import com.example.demo.cart.dto.res.CartRes;
import com.example.demo.cart.dto.res.PricePreviewRes;
import com.example.demo.cart.service.CartService;
import org.springframework.web.bind.annotation.*;

@RestController
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/cart/items")
    public CartRes addItem(@RequestBody CartAddReq request) {
        return cartService.add(request);
    }

    @GetMapping("/cart")
    public CartRes findCart() {
        return cartService.find();
    }

    @PatchMapping("/cart/items/{cartItemId}")
    public CartRes changeQuantity(@PathVariable Long cartItemId, @RequestBody CartQuantityReq request) {
        return cartService.changeQuantity(cartItemId, request);
    }

    @DeleteMapping("/cart/items/{cartItemId}")
    public void removeItem(@PathVariable Long cartItemId) {
        cartService.remove(cartItemId);
    }

    @GetMapping("/cart/preview")
    public PricePreviewRes preview(@RequestParam(required = false) String couponCode) {
        return cartService.preview(couponCode);
    }
}
