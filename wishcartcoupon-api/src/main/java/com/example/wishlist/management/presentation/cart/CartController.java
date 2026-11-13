package com.example.wishlist.management.presentation.cart;

import com.example.wishlist.management.application.cart.CartService;
import com.example.wishlist.management.dto.cart.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// presentation/CartController.java
@RestController
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) { this.cartService = cartService; }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartItemResponse add(@Valid @RequestBody CartAddRequest request) {
        return CartItemResponse.from(cartService.addItem(request.wishId(), request.quantity()));
    }

    @GetMapping("/items")
    public List<CartItemResponse> list() {
        return cartService.getItems().stream().map(CartItemResponse::from).toList();
    }

    @PatchMapping("/items/{itemId}")
    public CartItemResponse changeQuantity(@PathVariable @Min(1) long itemId,
                                           @Valid @RequestBody CartQuantityRequest request) {
        return CartItemResponse.from(cartService.changeQuantity(itemId, request.quantity()));
    }

    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable @Min(1) long itemId) {
        cartService.removeItem(itemId);
    }

    @GetMapping("/preview")
    public CartPreviewResponse preview(@RequestParam(defaultValue = "NONE") String coupon) {
        return CartPreviewResponse.from(cartService.preview(coupon));
    }

    @GetMapping("/coupon-recommendation")
    public CouponRecommendationResponse recommendCoupon() {
        return CouponRecommendationResponse.from(cartService.recommendCoupon());
    }
}