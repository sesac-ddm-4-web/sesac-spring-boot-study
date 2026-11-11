package com.example.demo.item.controller;

import com.example.demo.item.dto.req.CollectionItemReq;
import com.example.demo.item.dto.req.ExternalImportReq;
import com.example.demo.item.dto.res.CollectionItemRes;
import com.example.demo.item.service.CollectionItemImportService;
import com.example.demo.item.service.SimpleCollectionItemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CollectionItemController {

    private final SimpleCollectionItemService simpleCollectionItemService;
    private final CollectionItemImportService collectionItemImportService;

    CollectionItemController(SimpleCollectionItemService simpleCollectionItemService,
                             CollectionItemImportService collectionItemImportService){
        this.simpleCollectionItemService = simpleCollectionItemService;
        this.collectionItemImportService = collectionItemImportService;
    }

    @PostMapping(value = "/collectionItems")
    public CollectionItemRes createCollectionItem(@RequestBody CollectionItemReq request){
        return simpleCollectionItemService.add(request);
    }

    @PostMapping(value = "/collectionItems/import")
    public CollectionItemRes importExternalItem(@RequestBody ExternalImportReq request){
        return collectionItemImportService.importExternal(request);
    }

    @GetMapping(value = "/collectionItems/{id}")
    public CollectionItemRes findCollectionItemById(@PathVariable Long id){
        return simpleCollectionItemService.findById(id);
    }

    @GetMapping(value = "/collectionItems")
    public List<CollectionItemRes> findCollectionItems(){
        return simpleCollectionItemService.findAll();
    }

    @PutMapping(value = "/collectionItems/{id}")
    public CollectionItemRes updateCollectionItem(@PathVariable Long id, @RequestBody CollectionItemReq request){
        return simpleCollectionItemService.update(id, request);
    }

    @DeleteMapping(value = "/collectionItems/{id}")
    public void deleteCollectionItem(@PathVariable Long id){
        simpleCollectionItemService.delete(id);
    }

    @GetMapping(value = "/collectionItems/recommendations")
    public List<CollectionItemRes> recommendCollectionItems(@RequestParam Integer budget){
        return simpleCollectionItemService.recommend(budget);
    }
}
