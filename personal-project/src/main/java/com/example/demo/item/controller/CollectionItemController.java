package com.example.demo.controller;

import com.example.demo.dto.CollectionItemDto;
import com.example.demo.service.SimpleCollectionItemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CollectionItemController {

    private SimpleCollectionItemService simpleCollectionItemService;

    CollectionItemController(SimpleCollectionItemService simpleCollectionItemService){
        this.simpleCollectionItemService = simpleCollectionItemService;
    }

    @PostMapping(value = "/collectionItems")
    public CollectionItemDto createCollectionItem(@RequestBody CollectionItemDto collectionItemDto){
        return simpleCollectionItemService.add(collectionItemDto);
    }

    @GetMapping(value = "/collectionItems/{id}")
    public CollectionItemDto findCollectionItemById(@PathVariable Long id){
        return simpleCollectionItemService.findById(id);
    }

    @GetMapping(value = "/collectionItems")
    public List<CollectionItemDto> findCollectionItems(){
        return simpleCollectionItemService.findAll();
    }

    @PutMapping(value = "/collectionItems/{id}")
    public CollectionItemDto updateCollectionItem(@PathVariable Long id, @RequestBody CollectionItemDto collectionItemDto){
        CollectionItemDto collectionItemDtoWithId = new CollectionItemDto(
                id,
                collectionItemDto.name(),
                collectionItemDto.price()
        );
        return simpleCollectionItemService.update(collectionItemDtoWithId);
    }

    @DeleteMapping(value = "/collectionItems/{id}")
    public void deleteCollectionItem(@PathVariable Long id){
        simpleCollectionItemService.delete(id);
    }

    @GetMapping(value = "/collectionItems/recommendations")
    public List<CollectionItemDto> recommendCollectionItems(@RequestParam Integer budget){
        return simpleCollectionItemService.recommend(budget);
    }
}
