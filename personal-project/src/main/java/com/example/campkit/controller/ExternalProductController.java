
package com.example.campkit.controller;

import com.example.campkit.external.*;
import com.example.campkit.service.ExternalProductService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/external-products")
public class ExternalProductController {

    private final ExternalProductService service;

    public ExternalProductController(ExternalProductService service) {
        this.service = service;
    }

    @GetMapping
    public DummyJsonPage list(@RequestParam(defaultValue = "10") int limit, @RequestParam(defaultValue = "0") int skip) {
        return service.list(limit, skip);
    }

    @GetMapping("/search")
    public DummyJsonPage search(@RequestParam String q, @RequestParam(defaultValue = "10") int limit, @RequestParam(defaultValue = "0") int skip) {
        return service.search(q, limit, skip);
    }

    @GetMapping("/{id}")
    public DummyJsonProduct findById(@PathVariable long id) {
        return service.findById(id);
    }
}
