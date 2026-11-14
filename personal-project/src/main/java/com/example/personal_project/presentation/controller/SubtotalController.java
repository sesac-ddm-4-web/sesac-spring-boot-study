package com.example.personal_project.presentation.controller;

import com.example.personal_project.application.SubtotalService;
import com.example.personal_project.presentation.dto.shoppingCart.SubtotalRequest;
import com.example.personal_project.presentation.dto.shoppingCart.SubtotalResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubtotalController {
    private final SubtotalService subtotalService;

    public SubtotalController(SubtotalService subtotalService) {
        this.subtotalService = subtotalService;
    }

    @RequestMapping(value = "/shopping-cart/subtotal", method = {RequestMethod.GET, RequestMethod.POST})
    public SubtotalResponse getSubtotal(@RequestBody SubtotalRequest subtotalRequest){
        return subtotalService.getSubtotal(subtotalRequest);
    }
}
