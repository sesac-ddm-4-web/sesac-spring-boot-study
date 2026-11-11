package com.example.demo.item.repository;

import com.example.demo.item.domain.CollectionItem;

import java.util.List;

public interface CollectionItemRepository {
    CollectionItem add(CollectionItem collectionItem);
    CollectionItem findById(Long id);
    List<CollectionItem> findAll();
    CollectionItem update(CollectionItem collectionItem);
    void delete(Long id);
    boolean existsByExternalId(Long externalId);
}
