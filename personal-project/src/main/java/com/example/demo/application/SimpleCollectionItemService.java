package com.example.demo.application;

import com.example.demo.domain.CollectionItem;
import com.example.demo.domain.CollectionItemRepository;
import com.example.demo.presentation.CollectionItemDto;
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

    public CollectionItemDto add(CollectionItemDto collectionItemDto){
        CollectionItem collectionItem = CollectionItemDto.toEntity(collectionItemDto);
        validationService.checkValid(collectionItem);

        CollectionItem savedCollectionItem = collectionItemRepository.add(collectionItem);

        CollectionItemDto savedCollectionItemDto = CollectionItemDto.toDto(savedCollectionItem);

        return savedCollectionItemDto;
    }

    public CollectionItemDto findById(Long id){
        CollectionItem collectionItem = collectionItemRepository.findById(id);
        CollectionItemDto collectionItemDto = CollectionItemDto.toDto(collectionItem);
        return collectionItemDto;
    }

    public List<CollectionItemDto> findAll(){
        List<CollectionItem> collectionItems = collectionItemRepository.findAll();
        List<CollectionItemDto> collectionItemDtos = collectionItems.stream()
                .map(collectionItem -> CollectionItemDto.toDto(collectionItem))
                .toList();

        return collectionItemDtos;
    }

    public CollectionItemDto update(CollectionItemDto collectionItemDto){
        CollectionItem collectionItem = CollectionItemDto.toEntity(collectionItemDto);
        validationService.checkValid(collectionItem);

        CollectionItem updatedCollection = collectionItemRepository.update(collectionItem);
        CollectionItemDto updatedCollectionDto = CollectionItemDto.toDto(collectionItem);
        return updatedCollectionDto;
    }

    public void delete(Long id){
        collectionItemRepository.delete(id);
    }

    public List<CollectionItemDto> recommend(@NotNull @Min(0) Integer budget){
        return collectionItemRepository.findAll().stream()
                .filter(collectionItem -> collectionItem.getPrice() <= budget)
                .sorted(Comparator.comparing(CollectionItem::getPrice))
                .limit(3)
                .map(collectionItem -> CollectionItemDto.toDto(collectionItem))
                .toList();
    }
}
