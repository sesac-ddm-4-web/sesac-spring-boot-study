package com.example.hobby_wishlist.application;

import com.example.hobby_wishlist.domain.cart.CartRepository;
import com.example.hobby_wishlist.domain.externalproduct.ExternalProduct;
import com.example.hobby_wishlist.domain.externalproduct.ExternalProductProvider;
import com.example.hobby_wishlist.domain.wishlist.DuplicateWishlistItemException;
import com.example.hobby_wishlist.domain.wishlist.WishlistItem;
import com.example.hobby_wishlist.domain.wishlist.WishlistRepository;
import com.example.hobby_wishlist.dto.wishlist.CreateWishlistItemRequest;
import com.example.hobby_wishlist.dto.wishlist.WishlistItemResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishlistService {
    private final WishlistRepository wishlistRepository;
    private final ExternalProductProvider externalProductProvider;
    private final CartRepository cartRepository;

    public WishlistService(WishlistRepository wishlistRepository, ExternalProductProvider externalProductProvider, CartRepository cartRepository) {
        this.wishlistRepository = wishlistRepository;
        this.externalProductProvider = externalProductProvider;
        this.cartRepository = cartRepository;
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

    public synchronized WishlistItemResponse importExternal(Long externalId) {
        if (wishlistRepository.existsByExternalId(externalId)) {
            throw new DuplicateWishlistItemException("위시리스트에 이미 추가되어 있습니다.");
        }
        ExternalProduct externalProduct = externalProductProvider.findById(externalId);
        WishlistItem addedItem = wishlistRepository.add(WishlistItem.fromExternal(externalProduct));
        return WishlistItemResponse.from(addedItem);
    }

    @Transactional
    public void remove(Long id) {
        wishlistRepository.remove(id);
    }
}
