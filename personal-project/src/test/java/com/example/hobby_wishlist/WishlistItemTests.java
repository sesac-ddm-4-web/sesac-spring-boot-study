package com.example.hobby_wishlist;

import com.example.hobby_wishlist.domain.WishlistItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WishlistItemTests {
    @Test
    @DisplayName("이름의 앞뒤 공백은 제거됨")
    void stripNameTest() {
        WishlistItem item = WishlistItem.create(null, "  노트  ", 3000);
        assertEquals("노트", item.getName());
    }

    @Test
    @DisplayName("빈 이름, 음수 가격, null 가격은 거부함")
    void rejectInvalidTest() {
        assertThrows(IllegalArgumentException.class, () -> WishlistItem.create(null, null, 3000));
        assertThrows(IllegalArgumentException.class, () -> WishlistItem.create(null, " ", 3000));
        assertThrows(IllegalArgumentException.class, () -> WishlistItem.create(null, "노트", -1));
        assertThrows(IllegalArgumentException.class, () -> WishlistItem.create(null, "노트", null));
    }

    @Test
    @DisplayName("가격 0원은 허용하고, 예산과 같은 가격은 예산 이내로 판단함")
    void budgetBoundaryTest() {
        assertDoesNotThrow(() -> WishlistItem.create(null, "무료 샘플", 0));
        assertTrue(WishlistItem.create(null, "펜", 5000).isWithinBudget(5000));
        assertFalse(WishlistItem.create(null, "펜", 5001).isWithinBudget(5000));
    }
}
