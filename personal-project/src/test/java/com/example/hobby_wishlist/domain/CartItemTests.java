package com.example.hobby_wishlist.domain;

import com.example.hobby_wishlist.domain.cart.CartItem;
import com.example.hobby_wishlist.domain.cart.CartRuleViolationException;
import com.example.hobby_wishlist.domain.wishlist.WishlistItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CartItemTests {
    @Test
    @DisplayName("수량은 1 이상 5 이하여야 함")
    void quantityTest() {
        WishlistItem wishlistItem = WishlistItem.fromManual(0L, "의자", 5000);

        // 경계값 확인
        // 수량 1 또는 5 -> 통과
        assertDoesNotThrow(() -> CartItem.from(1L, wishlistItem, 1));
        assertDoesNotThrow(() -> CartItem.from(2L, wishlistItem, 5));

        // 수량 0 또는 6 -> 실패
        assertThrows(CartRuleViolationException.class, () -> CartItem.from(3L, wishlistItem, 0));
        assertThrows(CartRuleViolationException.class, () -> CartItem.from(4L, wishlistItem, 6));
    }

    @Test
    @DisplayName("수량은 재고 이하여야 함")
    void quantityLessThanStockTest() {
        int stock = 3;
        WishlistItem wishlistItem = WishlistItem.fromExternal(0L, 0L, "의자", 5000, stock);

        assertDoesNotThrow(() -> CartItem.from(1L, wishlistItem, 2));
        assertDoesNotThrow(() -> CartItem.from(1L, wishlistItem, 3));

        assertThrows(CartRuleViolationException.class, () -> CartItem.from(2L, wishlistItem, 4));
    }
}
