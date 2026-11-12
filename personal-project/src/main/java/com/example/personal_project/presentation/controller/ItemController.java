package com.example.personal_project.presentation.controller;

import com.example.personal_project.application.ImportItemService;
import com.example.personal_project.presentation.dto.item.ImportItemRequest;
import com.example.personal_project.presentation.dto.item.ItemDto;

import com.example.personal_project.application.ItemService;
import com.example.personal_project.domain.item.model.CollectionItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
// TODO: ImportItemRequest를 받는 POST /items/import를 ImportItemService에 연결한다. 기존 /items·/recommend 계약은 유지한다.
public class ItemController {

    private final ItemService itemService;
    private final ImportItemService importItemService;

    ItemController(
            ItemService itemService,
            ImportItemService importItemService
    ) {
        this.itemService = itemService;
        this.importItemService = importItemService;
    }

    @PostMapping("/items")
    public String registerItem(@RequestBody ItemDto itemDto) {
        itemService.add(itemDto);
        return "등록";
    }

    @GetMapping("/items")
    public List<ItemDto> findAllItems() {
        return itemService.findAll();
    }

    @GetMapping("/items/{id}")
    public CollectionItem findItemById(@PathVariable Long id) {
        return itemService.findById(id);
    }

    @GetMapping("/recommend")
    public List<CollectionItem> recommendItems(@RequestParam("budget") int budget) {
        return itemService.recommendItems(budget);
    }

    @PostMapping("/items/import")
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto importProduct(@RequestBody ImportItemRequest request) {
        return ItemDto.from(importItemService.importProduct(request.externalId()));
    }
}
