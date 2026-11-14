package com.example.wishlist.infrastructure;

import com.example.wishlist.domain.CollectionItem;
import com.example.wishlist.domain.CollectionItemRepository;
import com.example.wishlist.domain.SourceType;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ListCollectionItemRepository implements CollectionItemRepository {
    private List<CollectionItem> items = new ArrayList<>();
    private long nextId = 1;

    @Override
    public CollectionItem add(CollectionItem collectionItem) {
        collectionItem.setId(nextId++);
        items.add(collectionItem);
        return collectionItem;
    }

    @Override
    public CollectionItem findById(Long id) {
        for (CollectionItem item : items) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public CollectionItem findByExternalId(SourceType sourceType, Long externalId) {
        for (CollectionItem item : items) {
            if (item.getSourceType() == sourceType && externalId.equals(item.getExternalId())) {
                return item;
            }
        }
        return null;
    }

    @Override
    public List<CollectionItem> findAll() {
        return new ArrayList<>(items);
    }

    @Override
    public boolean delete(Long id) {
        return items.removeIf(item -> item.getId().equals(id));
    }
}
