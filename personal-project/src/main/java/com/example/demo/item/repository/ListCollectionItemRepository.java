package com.example.demo.repository;

import com.example.demo.domain.CollectionItem;
import com.example.demo.global.exception.EntityNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ListCollectionItemRepository implements CollectionItemRepository {

    private List<CollectionItem> items = new CopyOnWriteArrayList<>();
    private AtomicLong sequence = new AtomicLong(1L);

    public CollectionItem add(CollectionItem collectionItem){
        collectionItem.setId(sequence.getAndAdd(1L));

        items.add(collectionItem);
        return collectionItem;
    }

    public CollectionItem findById(Long id){
        return items.stream()
                .filter(collectionItem -> collectionItem.sameId(id))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Item을 찾지 못했습니다."));
    }

    public List<CollectionItem> findAll(){
        return List.copyOf(items);
    }

    public CollectionItem update(CollectionItem collectionItem){
        Integer indexToModify = items.indexOf(collectionItem);
        if (indexToModify == -1) {
            throw new EntityNotFoundException("Item을 찾지 못했습니다.");
        }
        items.set(indexToModify, collectionItem);
        return collectionItem;
    }

    public void delete(Long id){
        CollectionItem collectionItem = this.findById(id);
        items.remove(collectionItem);
    }
}
