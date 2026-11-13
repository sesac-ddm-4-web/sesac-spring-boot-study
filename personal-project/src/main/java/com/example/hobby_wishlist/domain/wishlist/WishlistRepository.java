package com.example.hobby_wishlist.domain.wishlist;


import java.util.List;

public interface WishlistRepository {
    WishlistItem add(WishlistItem item);

    List<WishlistItem> getAll();

    WishlistItem findById(Long id);

    List<WishlistItem> recommend(Integer budget);

    void remove(Long id);

    boolean existsByExternalId(Long externalId);
}
