package com.example.wishlist.item.service;

import com.example.wishlist.item.exception.InvalidBudgetException;
import com.example.wishlist.item.exception.InvalidItemException;
import com.example.wishlist.item.exception.ItemNotFoundException;
import com.example.wishlist.item.domain.WishlistItem;
import com.example.wishlist.item.repository.DatabaseWishlistItemRepository;
import com.example.wishlist.item.repository.WishlistItemRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WishlistService {

    private static final int MAX_RECOMMENDATION = 3;
    private final DatabaseWishlistItemRepository repository;

    public WishlistService(DatabaseWishlistItemRepository repository) {
        this.repository = repository;
    }

    // item 등록
    public WishlistItem register(String name, Integer price) {
        return repository.save(WishlistItem.manual(name, price));
    }

    // 전체 조회
    public List<WishlistItem> findAll() {
        return repository.findAll();
    }

    // 개별 조회
    public WishlistItem findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ItemNotFoundException(id));
    }

    // 이름, 가격 수정
    public WishlistItem update(Long id, String name, Integer price) {
        WishlistItem current = findById(id);
        return repository.update(current.changeTo(name, price));
    }

    // 삭제
    public void delete(Long id) {
        if (!repository.deleteById(id)) {
            throw new ItemNotFoundException(id);
        }
    }

    // 추천
    public List<WishlistItem> recommend(Integer budget) {
        if (budget == null || budget < 0) {
            throw new InvalidBudgetException(budget);
        }

        return repository.findAll().stream()
            .filter(item -> item.getPrice() <= budget)
            .sorted(Comparator.comparingInt(WishlistItem::getPrice))
            .limit(MAX_RECOMMENDATION)
            .toList();
    }

}
