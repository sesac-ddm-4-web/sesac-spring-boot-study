
package com.example.campkit.controller;

import com.example.campkit.domain.CollectionItem;
import com.example.campkit.service.CollectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/items")
public class CollectionController {

    private final CollectionService service;

    public CollectionController(CollectionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CollectionItem> register(@RequestParam String name, @RequestParam long price) {

        CollectionItem item = service.register(name, price);
        return ResponseEntity
                .created(URI.create("/api/items/" + item.getId()))
                .body(item);
    }

    @GetMapping
    public List<CollectionItem> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public CollectionItem findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/recommend")
    public List<CollectionItem> recommend(@RequestParam long budget) {
        return service.recommend(budget);
    }

    @PostMapping("/import/{externalId}")
    public ResponseEntity<CollectionItem> importExternal(@PathVariable long externalId) {

        CollectionItem item = service.importExternal(externalId);
        return ResponseEntity
                .created(URI.create("/api/items/" + item.getId()))
                .body(item);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
