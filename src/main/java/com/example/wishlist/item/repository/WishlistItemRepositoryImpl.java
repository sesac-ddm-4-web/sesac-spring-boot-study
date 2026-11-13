package com.example.wishlist.item.repository;

import com.example.wishlist.item.domain.WishlistItem;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;

//@Repository
public class WishlistItemRepositoryImpl implements WishlistItemRepository{

    private final Map<Long, WishlistItem> store = new LinkedHashMap<>();
    private long sequence = 0L;

    @Override
    public synchronized WishlistItem save(WishlistItem item) {
        WishlistItem saved = item.withId(++sequence);
        store.put(saved.getId(), saved);
        return saved;
    }

    @Override
    public synchronized WishlistItem update(WishlistItem item) {
        if (!store.containsKey(item.getId())) {
            throw new IllegalArgumentException("저장되지 않은 아이템은 수정할 수 없습니다. id=" + item.getId());
        }
        store.put(item.getId(), item);
        return item;
    }

    @Override
    public synchronized boolean deleteById(Long id) {
        return store.remove(id) != null;
    }

    @Override
    public synchronized List<WishlistItem> findAll() {
        return List.copyOf(new ArrayList<>(store.values()));
    }

    @Override
    public synchronized Optional<WishlistItem> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public synchronized Optional<WishlistItem> findByExternalId(Long externalId) {
        return store.values().stream()
            .filter(item -> Objects.equals(item.getExternalId(), externalId))
            .findFirst();
    }
}
