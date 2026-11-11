package com.example.demo.item.service;

import com.example.demo.item.domain.CollectionItem;
import com.example.demo.item.dto.req.CollectionItemReq;
import com.example.demo.item.dto.res.CollectionItemRes;
import com.example.demo.item.repository.CollectionItemRepository;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Comparator;
import java.util.List;

@Service
@Validated
public class SimpleCollectionItemService {

    private CollectionItemRepository collectionItemRepository;
    private ValidationService validationService;

    SimpleCollectionItemService(CollectionItemRepository collectionItemRepository, ValidationService validationService){
        this.collectionItemRepository = collectionItemRepository;
        this.validationService = validationService;
    }

    public CollectionItemRes add(CollectionItemReq request){
        CollectionItem collectionItem = request.toEntity(null);   // 등록이라 id는 null
        validationService.checkValid(collectionItem);
        CollectionItem saved = collectionItemRepository.add(collectionItem);
        return CollectionItemRes.from(saved);
    }

    public CollectionItemRes findById(Long id){
        return CollectionItemRes.from(collectionItemRepository.findById(id));
    }

    public List<CollectionItemRes> findAll(){
        return collectionItemRepository.findAll().stream()
                .map(CollectionItemRes::from)
                .toList();
    }

    public CollectionItemRes update(Long id, CollectionItemReq request){
        CollectionItem existing = collectionItemRepository.findById(id);
        existing.checkEditable();

        CollectionItem collectionItem = request.toEntity(id);
        validationService.checkValid(collectionItem);
        collectionItemRepository.update(collectionItem);
        return CollectionItemRes.from(collectionItem);
    }

    public void delete(Long id){
        collectionItemRepository.delete(id);
    }

    public List<CollectionItemRes> recommend(@NotNull @Min(0) Integer budget){
        return collectionItemRepository.findAll().stream()
                .filter(collectionItem -> collectionItem.getPrice() <= budget)
                .sorted(Comparator.comparing(CollectionItem::getPrice))
                .limit(3)
                .map(CollectionItemRes::from)
                .toList();
    }
}
