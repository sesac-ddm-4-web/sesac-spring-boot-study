package com.example.hobby_wishlist;

import com.example.hobby_wishlist.domain.wishlist.WishlistItemNotFoundException;
import com.example.hobby_wishlist.domain.wishlist.WishlistItem;
import com.example.hobby_wishlist.infrastructure.persistence.inmemory.InMemoryWishListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryWishListRepositoryUnitTests {
    private InMemoryWishListRepository wishListRepository;

    @BeforeEach
    void setUp() {
        wishListRepository = new InMemoryWishListRepository();
    }

    @Test
    @DisplayName("존재하지 않는 id 조회 시 예외 발생")
    void findNonExistentId() {
        WishlistItem item1 = wishListRepository.add(WishlistItem.fromManual(null, "연필", 300));
        WishlistItem item2 = wishListRepository.add(WishlistItem.fromManual(null, "노트", 1000));

        assertDoesNotThrow(() -> wishListRepository.findById(item1.getId()));
        assertDoesNotThrow(() -> wishListRepository.findById(item2.getId()));

        Long nonExistentId = Math.max(item1.getId(), item2.getId()) + 1L;
        assertThrows(WishlistItemNotFoundException.class, () -> wishListRepository.findById(nonExistentId));
    }

    @Test
    @DisplayName("추천 시 상위 3개 추천 및 정렬 순서 유지")
    void recommendTest() {
        WishlistItem item1 = wishListRepository.add(WishlistItem.fromManual(null, "항목1", 500));
        WishlistItem item2 = wishListRepository.add(WishlistItem.fromManual(null, "항목2", 2000));
        WishlistItem item3 = wishListRepository.add(WishlistItem.fromManual(null, "항목3", 300));
        WishlistItem item4 = wishListRepository.add(WishlistItem.fromManual(null, "항목4", 500));
        WishlistItem item5 = wishListRepository.add(WishlistItem.fromManual(null, "항목5", 1000));
        WishlistItem item6 = wishListRepository.add(WishlistItem.fromManual(null, "항목6", 700));

        var actual = wishListRepository.recommend(800);
        var expected = List.of(item3, item1, item4);

        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertSame(expected.get(i), actual.get(i));
        }
    }
}
