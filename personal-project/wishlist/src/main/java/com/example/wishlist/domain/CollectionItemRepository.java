package com.example.wishlist.domain;

import java.util.List;

public interface CollectionItemRepository {

    CollectionItem add(CollectionItem collectionItem);

    CollectionItem findById(Long id);

    List<CollectionItem> findAll();
}
