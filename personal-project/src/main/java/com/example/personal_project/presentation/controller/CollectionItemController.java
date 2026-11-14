package com.example.personal_project.presentation.controller;

import com.example.personal_project.application.CollectionItemService;
import com.example.personal_project.infrastructure.DummyJsonClient;
import com.example.personal_project.presentation.dto.collectionItem.CollectionItemRequest;
import com.example.personal_project.presentation.dto.collectionItem.CollectionItemResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/item")
public class CollectionItemController {
    private final CollectionItemService collectionItemService;
    private final DummyJsonClient dummyJsonClient;

    public CollectionItemController(CollectionItemService collectionItemService, DummyJsonClient dummyJsonClient) {
        this.collectionItemService = collectionItemService;
        this.dummyJsonClient = dummyJsonClient;
    }

    @PostMapping
    public CollectionItemResponse createItem(@Valid @RequestBody CollectionItemRequest collectionItemRequest){
        return collectionItemService.internalAdd(collectionItemRequest);
    }

    @GetMapping
    public List<CollectionItemResponse> findItems(){
        return collectionItemService.findAll();
    }

    @GetMapping("/{id}")
    public CollectionItemResponse findItemById(@PathVariable Long id){
        return collectionItemService.findItemById(id);
    }

    @GetMapping("/recommendations")
    public List<CollectionItemResponse> findItemsWithBudget(
            @RequestParam
            @Min(value = 0, message = "예산은 0 이상입니다.")
            Integer budget
    ){
        return collectionItemService.findItemsWithBudget(budget);
    }

    @PostMapping("/external/{id}")
    public CollectionItemResponse externalItemAdd(@PathVariable Long id){
        return collectionItemService.externalAdd(dummyJsonClient.findById(id));
    }
}