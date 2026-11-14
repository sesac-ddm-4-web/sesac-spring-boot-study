package com.example.personal_project.presentation.controller;

import com.example.personal_project.application.ShoppingCartService;
import com.example.personal_project.presentation.dto.shoppingCart.ShoppingCartRequest;
import com.example.personal_project.presentation.dto.shoppingCart.ShoppingCartResponse;
import com.example.personal_project.presentation.dto.shoppingCart.SubtotalRequest;
import com.example.personal_project.presentation.dto.shoppingCart.SubtotalResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shopping-cart")
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @PostMapping()
    public ShoppingCartResponse addCart(@Valid @RequestBody ShoppingCartRequest shoppingCartRequest){
        return shoppingCartService.addCart(shoppingCartRequest);
    }

    @PatchMapping()
    public ShoppingCartResponse editCart(@Valid @RequestBody ShoppingCartRequest shoppingCartRequest){
        return shoppingCartService.editCart(shoppingCartRequest);
    }

    @GetMapping()
    public List<ShoppingCartResponse> getAllCart(){
        return shoppingCartService.findAll();
    }

    @DeleteMapping("/{collectionItemId}")
    public void deleteCart(@PathVariable Long collectionItemId){
        shoppingCartService.delete(collectionItemId);
    }
}
