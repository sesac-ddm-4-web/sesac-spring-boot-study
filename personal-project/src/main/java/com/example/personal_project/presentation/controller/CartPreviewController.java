package com.example.personal_project.presentation.controller;

import com.example.personal_project.application.CartService;
import com.example.personal_project.presentation.dto.cart.CartPreviewResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartPreviewController {
    private final CartService cartService;

    public CartPreviewController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/best-coupon")
    public CartPreviewResponse bestCoupon() {
        return CartPreviewResponse.from(cartService.bestCoupon());
    }

    @GetMapping("/preview")
    public CartPreviewResponse preview(
            @RequestParam(name = "couponCode", required = false) String couponCode) {
        return CartPreviewResponse.from(cartService.preview(couponCode));
    }
}
