package com.example.wishlist.cart.controller;

import com.example.wishlist.cart.dto.BestCouponResponse;
import com.example.wishlist.cart.dto.CartAddRequest;
import com.example.wishlist.cart.dto.CartItemResponse;
import com.example.wishlist.cart.dto.CartQuantityRequest;
import com.example.wishlist.cart.dto.CartResponse;
import com.example.wishlist.cart.dto.PricePreviewResponse;
import com.example.wishlist.cart.service.CartAddResult;
import com.example.wishlist.cart.service.CartService;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private static final String CART_ITEMS_PATH = "/api/cart/items/";

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(CartResponse.from(service.getCart()));
    }

    @GetMapping("/items/{cartItemId}")
    public ResponseEntity<CartItemResponse> getItem(@PathVariable("cartItemId") Long cartItemId) {
        return ResponseEntity.ok(CartItemResponse.from(service.getCart().getItem(cartItemId)));
    }

    @PostMapping("/items")
    public ResponseEntity<CartItemResponse> add(@RequestBody CartAddRequest request) {
        request.validate();
        CartAddResult result = service.add(request.wishlistItemId(), request.quantity());
        CartItemResponse response = CartItemResponse.from(result.item());

        if (result.newLine()) {
            return ResponseEntity.created(URI.create(CART_ITEMS_PATH + response.id())).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<CartItemResponse> changeQuantity(@PathVariable("cartItemId") Long cartItemId,
        @RequestBody CartQuantityRequest request) {
        request.validate();
        return ResponseEntity.ok(CartItemResponse.from(
            service.changeQuantity(cartItemId, request.quantity())));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> remove(@PathVariable("cartItemId") Long cartItemId) {
        service.remove(cartItemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/preview")
    public ResponseEntity<PricePreviewResponse> preview(
        @RequestParam(name = "couponCode", required = false) String couponCode) {
        return ResponseEntity.ok(PricePreviewResponse.from(service.preview(couponCode)));
    }

    @GetMapping("/best-coupon")
    public ResponseEntity<BestCouponResponse> bestCoupon() {
        return ResponseEntity.ok(BestCouponResponse.from(service.findBestCoupon()));
    }
}
