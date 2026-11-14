package com.example.personal_project.infrastructure;

import com.example.personal_project.domain.Item;
import com.example.personal_project.domain.ItemRepository;
import com.example.personal_project.domain.EntityNotFoundException;
import com.example.personal_project.domain.SourceType;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class MemoryItemRepository implements ItemRepository {
    private List<Item> items = new CopyOnWriteArrayList<>();
    private AtomicLong sequence = new AtomicLong(1L);

    @Override
    public Item add(Item item) {
        Item savedItem = new Item(
                sequence.getAndAdd(1L),
                item.getName(),
                item.getPrice(),
                item.getSourceType(),
                item.getExternalId(),
                item.getStock()
        );
        items.add(savedItem);
        return savedItem;
    }

    @Override
    public Item findById(Long id) {
        return items.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("item을 찾지 못했습니다."));
    }

    @Override
    public List<Item> findAll() {
        List<Item> itemList = new ArrayList<>(items);
        return itemList;
    }

    @Override
    public List<Item> findByNameContaining(String name) {
        return items.stream()
                .filter(item -> item.getName().contains(name))
                .toList();
    }

    @Override
    public boolean existsExternal(SourceType sourceType,Long externalId) {
        return items.stream()
                // 출처가 같은 아이템만 남김
                .filter(item -> item.getSourceType().equals(sourceType))
                // 외부 id까지 같은 아이템만 남김
                .filter(item -> Objects.equals(item.getExternalId(), externalId))
                // 남은 아이템이 하나라도 있으면 true
                .anyMatch(item -> true);
    }

    @Override
    public Item update(Item item) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(item.getId())) {
                items.set(i, item);
                return item;
            }
        }
        throw new EntityNotFoundException("아이템을 찾지 못했습니다.");
    }
}
