package com.example.sesac_spring_boot_study.product;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/external-products")
public class ExternalProductController {
    private final DummyJsonClient client;

    public ExternalProductController(DummyJsonClient client) {
        this.client = client;
    }

    @GetMapping("/{id}")
    public DummyJsonProduct findById(@PathVariable("id") long id) {
        return client.findById(id);
    }

    @GetMapping()
    public DummyJsonProductList list(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int skip) {

        if (limit < 1 || limit > 20) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "limit은 1~20이어야 합니다");
        }
        if (skip < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "skip은 0 이상이어야 합니다");
        }
        return client.list(limit, skip);
    }
    @GetMapping("/search")
    public DummyJsonProductList search (
            @RequestParam String q) {
        if (q==null || q.isBlank()){
            throw new IllegalArgumentException("이름은 비어 있을 수 없습니다.");
        }
        return client.search(q.trim());
    }


}
