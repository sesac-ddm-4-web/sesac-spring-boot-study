
package com.example.campkit.controller;

import com.example.campkit.domain.CartItem;
import com.example.campkit.dto.*;
import com.example.campkit.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @GetMapping
    public List<CartItem> findAll() {
        return service.findAll();
    }

    @PostMapping
    public ResponseEntity<CartItem> add(@Valid @RequestBody AddCartRequest request) {

        CartItem item = service.add(request.itemId(), request.quantity());
        return ResponseEntity
                .created(URI.create("/api/cart/" + item.getId()))
                .body(item);
    }

    @PatchMapping("/{id}")
    public CartItem update(@PathVariable long id, @Valid @RequestBody UpdateQuantityRequest request) {
        return service.update(id, request.quantity());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/preview")
    public CartPreview preview(@RequestParam(defaultValue = "NONE") String couponCode) {

        return service.preview(couponCode);
    }

    @GetMapping("/{id}")
    public CartItem findById(@PathVariable long id) {
        return service.findById(id);
    }

}
