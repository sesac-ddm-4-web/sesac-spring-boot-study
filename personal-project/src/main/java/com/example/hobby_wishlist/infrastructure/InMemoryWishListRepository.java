package com.example.hobby_wishlist.infrastructure;

import com.example.hobby_wishlist.domain.WishlistItem;
import com.example.hobby_wishlist.domain.EntityNotFoundException;
import com.example.hobby_wishlist.domain.WishlistRepository;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryWishListRepository implements WishlistRepository {
    private final List<WishlistItem> items = new CopyOnWriteArrayList<>();
    private final AtomicLong sequence = new AtomicLong(1L);

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
                .filter(item -> item.sameId(id))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("아이템을 찾을 수 없습니다."));
    }

    @Override
    public List<WishlistItem> recommend(Integer budget) {
        return items.stream()
                .filter(item -> item.isWithinBudget(budget))
                .sorted(Comparator.comparing(WishlistItem::getPrice))
                .limit(3)
                .toList();
    }
}
