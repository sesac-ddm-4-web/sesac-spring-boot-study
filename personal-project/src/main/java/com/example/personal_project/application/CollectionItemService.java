package com.example.personal_project.application;

import com.example.personal_project.domain.entity.CollectionItem;
import com.example.personal_project.domain.repository.CollectionItemRepository;
import com.example.personal_project.domain.exception.CommonConflictException;
import com.example.personal_project.presentation.dto.collectionItem.CollectionItemRequest;
import com.example.personal_project.presentation.dto.collectionItem.CollectionItemResponse;
import com.example.personal_project.presentation.dto.dummyJsonProduct.DummyJsonProduct;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class CollectionItemService {
    private final CollectionItemRepository collectionItemRepository;
    private final ValidationService validationService;

    public CollectionItemService(CollectionItemRepository collectionItemRepository, ValidationService validationService) {
        this.collectionItemRepository = collectionItemRepository;
        this.validationService = validationService;
    }

    public CollectionItemResponse internalAdd(CollectionItemRequest collectionItemRequest){
        CollectionItem collectionItem = CollectionItem.createInternalItem(collectionItemRequest);
        validationService.checkValid(collectionItem);
        CollectionItem savedCollectionItem = collectionItemRepository.add(collectionItem);
        return CollectionItemResponse.form(savedCollectionItem);
    }

    public CollectionItemResponse externalAdd(DummyJsonProduct externalItem){
        if(collectionItemRepository.existsByExternalId(externalItem.id())){
            throw new CommonConflictException("이미 추가한 외부 item입니다");
        }
        CollectionItem collectionItem = CollectionItem.createExternalItem(externalItem);
        validationService.checkValid(collectionItem);
        CollectionItem savedCollectionItem = collectionItemRepository.add(collectionItem);
        return CollectionItemResponse.form(savedCollectionItem);
    }

    public List<CollectionItemResponse> findAll(){
        List<CollectionItem> items = collectionItemRepository.findAll();
        return items.stream()
                .map(CollectionItemResponse::form)
                .toList();
    }

    public CollectionItemResponse findItemById(Long id){
        CollectionItem findCollectionItem = collectionItemRepository.findById(id);
        return CollectionItemResponse.form(findCollectionItem);
    }

    public List<CollectionItemResponse> findItemsWithBudget(Integer budget){
        return collectionItemRepository.findItemsWithBudget(budget,CollectionItem.RECOMMEND_NUM).stream()
                .map(CollectionItemResponse::form)
                .toList();
    }
}
