package com.example.wishlist.domain;

import java.util.List;

public interface CollectionItemRepository {

    CollectionItem add(CollectionItem collectionItem);

    CollectionItem findById(Long id);

    CollectionItem findByExternalId(SourceType sourceType, Long externalId);

    List<CollectionItem> findAll();

    CollectionItem update(CollectionItem collectionItem);

    boolean delete(Long id);
}
