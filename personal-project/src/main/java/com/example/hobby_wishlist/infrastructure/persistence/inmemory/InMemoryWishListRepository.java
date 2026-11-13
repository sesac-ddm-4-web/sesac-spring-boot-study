package com.example.hobby_wishlist.infrastructure.persistence.inmemory;

import com.example.hobby_wishlist.domain.wishlist.WishlistItem;
import com.example.hobby_wishlist.domain.wishlist.WishlistItemNotFoundException;
import com.example.hobby_wishlist.domain.wishlist.WishlistRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@Profile("test")
public class InMemoryWishListRepository implements WishlistRepository {
    private final List<WishlistItem> items = new CopyOnWriteArrayList<>();
    private final AtomicLong sequence = new AtomicLong(0L);

    @Override
    public WishlistItem add(WishlistItem item) {
        item.setId(sequence.getAndIncrement());
        items.add(item);
        return item;
    }

    @Override
    public List<WishlistItem> getAll() {
        return List.copyOf(items);
    }

    @Override
    public WishlistItem findById(Long id) {
        return items.stream()
                .filter(item -> item.hasSameId(id))
                .findFirst()
                .orElseThrow(() -> new WishlistItemNotFoundException("아이템을 찾을 수 없습니다."));
    }

    @Override
    public List<WishlistItem> recommend(Integer budget) {
        return items.stream()
                .filter(item -> item.isWithinBudget(budget))
                .sorted(Comparator.comparing(WishlistItem::getPrice))
                .limit(3)
                .toList();
    }

    @Override
    public void remove(Long id) {
        items.removeIf(item -> item.hasSameId(id));
    }

    @Override
    public boolean existsByExternalId(Long externalId) {
        return items.stream()
                .anyMatch(item -> item.isExternal(externalId));
    }
}
