package com.example.hobby_wishlist.application;

import com.example.hobby_wishlist.domain.WishlistItem;
import com.example.hobby_wishlist.domain.WishlistRepository;
import com.example.hobby_wishlist.presentation.WishlistItemDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishlistService {
    private final WishlistRepository wishlistRepository;

    public WishlistService(WishlistRepository wishlistRepository) {
        this.wishlistRepository = wishlistRepository;
    }

    public WishlistItemDto add(WishlistItemDto itemDto) {
        WishlistItem addedItem = wishlistRepository.add(itemDto.toEntity());
        return WishlistItemDto.create(addedItem);
    }

    public List<WishlistItemDto> getAll() {
        return wishlistRepository.getAll().stream()
                .map(WishlistItemDto::create)
                .toList();
    }

    public WishlistItemDto findById(Long id) {
        WishlistItem item = wishlistRepository.findById(id);
        return WishlistItemDto.create(item);
    }

    public List<WishlistItemDto> recommend(Integer budget) {
        if (budget < 0) {
            throw new IllegalArgumentException("예산은 0 이상이어야 합니다.");
        }

        return wishlistRepository.recommend(budget).stream()
                .map(WishlistItemDto::create)
                .toList();
    }
}
