package com.example.sesac_spring_boot_study.presentation;

import com.example.sesac_spring_boot_study.application.CartService;
import com.example.sesac_spring_boot_study.domain.CartItem;
import com.example.sesac_spring_boot_study.domain.CartPreview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/cart")
public class CartController {

    private CartService cartService;

    @Autowired
    CartController (CartService cartService){
        this.cartService = cartService;
    }

    public record AddRequest(Long productId, int quantity) {}
    public record QuantityRequest(int quantity) {} //안묶었더니 오류나더라...

    @PostMapping("/items")
    public CartItem add(@RequestBody AddRequest request) {
        return cartService.addItem(request.productId(), request.quantity());
    }

    @GetMapping
    public List<CartItem> getItems(){
        return cartService.getItems();
    }

    @PutMapping("/items/{rowId}")
    public CartItem changeQuantity(@PathVariable Long rowId, @RequestBody QuantityRequest request) {
        return cartService.changeQuantity(rowId, request.quantity());
    }

    @DeleteMapping("/items/{rowId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long rowId) {
        cartService.remove(rowId);
    }

    @GetMapping("/preview")
    public CartPreview preview(@RequestParam(required = false) String couponCode) {
        return cartService.preview(couponCode);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException e) {
        return e.getMessage();
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoSuchElementException e) {
        return e.getMessage();
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleConflict(IllegalStateException e) {
        return e.getMessage();
    }


}
