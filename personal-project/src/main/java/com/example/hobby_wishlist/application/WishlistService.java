package com.example.hobby_wishlist.application;

import com.example.hobby_wishlist.domain.ExternalProduct;
import com.example.hobby_wishlist.domain.ExternalProductProvider;
import com.example.hobby_wishlist.domain.WishlistItem;
import com.example.hobby_wishlist.domain.WishlistRepository;
import com.example.hobby_wishlist.dto.CreateWishlistItemRequest;
import com.example.hobby_wishlist.dto.WishlistItemResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishlistService {
    private final WishlistRepository wishlistRepository;
    private final ExternalProductProvider externalProductProvider;

    public WishlistService(WishlistRepository wishlistRepository, ExternalProductProvider externalProductProvider) {
        this.wishlistRepository = wishlistRepository;
        this.externalProductProvider = externalProductProvider;
    }

    public WishlistItemResponse add(CreateWishlistItemRequest itemDto) {
        WishlistItem addedItem = wishlistRepository.add(itemDto.toEntity());
        return WishlistItemResponse.from(addedItem);
    }

    public List<WishlistItemResponse> getAll() {
        return wishlistRepository.getAll().stream()
                .map(WishlistItemResponse::from)
                .toList();
    }

    public WishlistItemResponse findById(Long id) {
        WishlistItem item = wishlistRepository.findById(id);
        return WishlistItemResponse.from(item);
    }

    public List<WishlistItemResponse> recommend(Integer budget) {
        if (budget < 0) {
            throw new IllegalArgumentException("예산은 0 이상이어야 합니다.");
        }

        return wishlistRepository.recommend(budget).stream()
                .map(WishlistItemResponse::from)
                .toList();
    }

    public WishlistItemResponse importExternal(Long externalId) {
        ExternalProduct externalProduct = externalProductProvider.findById(externalId);
        WishlistItem addedItem = wishlistRepository.add(WishlistItem.fromExternal(externalProduct));
        return WishlistItemResponse.from(addedItem);
    }
}
