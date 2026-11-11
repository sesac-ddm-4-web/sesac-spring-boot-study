package com.example.demo.repository;

import com.example.demo.domain.CollectionItem;

import java.util.List;

public interface CollectionItemRepository {
    CollectionItem add(CollectionItem collectionItem);
    CollectionItem findById(Long id);
    List<CollectionItem> findAll();
    CollectionItem update(CollectionItem collectionItem);
    void delete(Long id);
}
