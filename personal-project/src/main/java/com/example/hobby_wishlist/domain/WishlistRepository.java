package com.example.hobby_wishlist.domain;


import java.util.List;

public interface WishlistRepository {
    WishlistItem add(WishlistItem item);

    List<WishlistItem> getAll();

    WishlistItem findById(Long id);

    List<WishlistItem> recommend(Integer budget);
}
