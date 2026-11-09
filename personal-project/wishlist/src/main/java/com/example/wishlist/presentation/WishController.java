package com.example.wishlist.presentation;

import com.example.wishlist.application.WishService;
import com.example.wishlist.domain.CollectionItem;
import com.example.wishlist.domain.ItemNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestController
public class WishController {
    private final WishService service;

    public WishController(WishService service) {
        this.service = service;
    }

    @PostMapping("/wish/items")
    public CollectionItem register(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "price", required = false) Integer price) {
        return service.register(name, price);
    }

    @GetMapping("/wish/items")
    public List<CollectionItem> findAll() {
        return service.findAll();
    }

    @GetMapping("/wish/items/{id}")
    public CollectionItem findById(@PathVariable("id") Long id) {
        return service.findById(id);
    }

    @GetMapping("/wish/recommendations")
    public List<CollectionItem> recommend(
            @RequestParam(name = "budget", required = false) Integer budget) {
        return service.recommend(budget);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getName() + "은(는) 정수로 입력해 주세요.");
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ItemNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
