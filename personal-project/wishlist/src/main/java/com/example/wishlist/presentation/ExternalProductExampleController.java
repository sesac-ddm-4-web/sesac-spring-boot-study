package com.example.wishlist.presentation;

import com.example.wishlist.infrastructure.DummyJsonClient;
import com.example.wishlist.infrastructure.DummyJsonProduct;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/examples/external-products")
public class ExternalProductExampleController {
    private final DummyJsonClient client;

    public ExternalProductExampleController(DummyJsonClient client) {
        this.client = client;
    }

    @GetMapping("/{id}")
    public DummyJsonProduct findById(@PathVariable("id") long id) {
        return client.findById(id);
    }
}
