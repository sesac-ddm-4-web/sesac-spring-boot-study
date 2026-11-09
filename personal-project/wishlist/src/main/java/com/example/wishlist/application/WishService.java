package com.example.wishlist.application;

import com.example.wishlist.domain.CollectionItem;
import com.example.wishlist.domain.CollectionItemRepository;
import com.example.wishlist.domain.ItemNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class WishService {
    private static final int MAX_RECOMMENDATIONS = 3;

    private final CollectionItemRepository repository;

    public WishService(CollectionItemRepository repository) {
        this.repository = repository;
    }

    public CollectionItem register(String name, Integer price) {
        if (name == null || name.strip().isEmpty()) {
            throw new IllegalArgumentException("이름을 입력해 주세요.");
        }
        if (price == null) {
            throw new IllegalArgumentException("가격을 입력해 주세요.");
        }
        if (price < 0) {
            throw new IllegalArgumentException("가격은 0원 이상이어야 합니다.");
        }
        CollectionItem collectionItem = new CollectionItem(name.strip(), price);
        return repository.add(collectionItem);
    }

    public List<CollectionItem> findAll() {
        return repository.findAll();
    }

    public CollectionItem findById(Long id) {
        CollectionItem item = repository.findById(id);
        if (item == null) {
            throw new ItemNotFoundException(id);
        }
        return item;
    }

    public List<CollectionItem> recommend(Integer budget) {
        if (budget == null) {
            throw new IllegalArgumentException("예산을 입력해 주세요.");
        }
        if (budget < 0) {
            throw new IllegalArgumentException("예산은 0원 이상이어야 합니다.");
        }
        return repository.findAll().stream()
                .filter(item -> item.getPrice() <= budget)
                .sorted(Comparator.comparingInt(CollectionItem::getPrice))
                .limit(MAX_RECOMMENDATIONS)
                .toList();
    }
}
