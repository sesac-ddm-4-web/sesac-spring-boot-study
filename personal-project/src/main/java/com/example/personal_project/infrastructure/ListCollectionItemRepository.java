package com.example.personal_project.infrastructure;

import com.example.personal_project.domain.entity.CollectionItem;
import com.example.personal_project.domain.repository.CollectionItemRepository;
import com.example.personal_project.domain.exception.CommonNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ListCollectionItemRepository implements CollectionItemRepository {
    private List<CollectionItem> collectionItems = new CopyOnWriteArrayList<>();
    AtomicLong sequence = new AtomicLong(1L);

    public CollectionItem add(CollectionItem collectionItem){
        collectionItem.setId(sequence.getAndAdd(1L));
        collectionItems.add(collectionItem);
        return collectionItem;
    }

    public List<CollectionItem> findAll(){
        return collectionItems;
    }

    public CollectionItem findById(Long id){
        return collectionItems.stream()
                .filter(item->item.sameId(id))
                .findFirst()
                .orElseThrow(()->new CommonNotFoundException("해당 id로 item을 찾을 수 없습니다"));
    }

    public Boolean existsByExternalId(Long externalId) {
        return collectionItems.stream()
                .anyMatch(item ->
                        Objects.equals(item.getExternalId(), externalId));
    }

    public List<CollectionItem> findItemsWithBudget(Integer budget, Integer limit){
        return collectionItems.stream()
                .filter(item -> item.getPrice()<=budget)
                .sorted(Comparator.comparingInt(CollectionItem::getPrice))
                .limit(limit)
                .toList();
    }
}
