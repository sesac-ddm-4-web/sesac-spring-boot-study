package com.example.personal_project.presentation;

import com.example.personal_project.application.ItemService;
import com.example.personal_project.domain.Item;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    // 상품 등록
    @PostMapping
    public ItemDto add(
            @RequestBody ItemDto itemDto) {
        return itemService.add(itemDto);
    }

    // 상품 id로 조회
    @GetMapping("/{id}")
    public ItemDto findById(@PathVariable Long id){
        return itemService.findById(id);
    }

    // 상품 전체 조회
    @GetMapping
    public List<ItemDto> findAll() {
        return itemService.findAll();
    }

    // 추천
    @GetMapping("/recommend")
    public List<ItemDto> recommend(@RequestParam int budget) {
        return itemService.recommend(budget);
    }

    // 포함 문자열 검색
    @GetMapping("/search")
    public List<ItemDto> findItem(
            @RequestParam(required = false) String name) {
        return itemService.findByNameContaining(name);
    }

    // 외부 상품 가져오기
    @PostMapping("/import/{externalId}")
    public ItemDto importExternal(
            @PathVariable("externalId") long externalId) {
        return itemService.importExternal(externalId);
    }

    // 수동 이름 가격 수정
    @PutMapping("/{id}")
    public ItemDto update(
            @PathVariable("id") Long id,
            @RequestBody ItemDto itemDto) {
        return itemService.update(id, itemDto);
    }

}
