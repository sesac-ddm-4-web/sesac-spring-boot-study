package com.example.personal_project.infrastructure.repository;

import com.example.personal_project.domain.item.model.CollectionItem;
import com.example.personal_project.domain.item.model.SourceType;
import com.example.personal_project.domain.item.repository.ItemRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ListItemRepository implements ItemRepository {

    private List<CollectionItem> collectionItems = new ArrayList<>();
    private AtomicLong sequence = new AtomicLong(1L);

    @Override
    public CollectionItem add(CollectionItem item) {
        CollectionItem savedItem = new CollectionItem(
                sequence.getAndIncrement(),
                item.name(),
                item.price(),
                item.sourceType(),
                item.externalId(),
                item.stock()
        );

        collectionItems.add(savedItem);
        return savedItem;
    }


    @Override
    public List<CollectionItem> findAll() {
        return List.copyOf(collectionItems);
    }

    @Override
    public CollectionItem findById(Long id) {
        return collectionItems.stream()
                .filter(collectionItem -> collectionItem.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다. ID: " + id));
    }

    @Override
    public boolean existsBySourceTypeAndExternalId(SourceType sourceType, Long externalId) {
        return collectionItems.stream()
                .anyMatch(item ->
                        item.sourceType() == sourceType
                                && Objects.equals(item.externalId(), externalId)
                );
    }
}
