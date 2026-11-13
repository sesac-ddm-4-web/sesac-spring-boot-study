package com.example.wishlist.management.domain.cart;

import com.example.wishlist.management.domain.exception.ConflictException;
import com.example.wishlist.management.domain.wish.Wish;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class CartTest {
    private Wish manual(long id, int price) { return new Wish(id, "상품" + id, price); }
    private Wish external(long id, int price, int stock) {
        Wish wish = Wish.external(id, "외부" + id, price, stock);
        wish.setId(id);
        return wish;
    }

    @Test
    void mergesQuantityOfSameWish() {
        Cart cart = new Cart();
        cart.add(manual(1, 1_000), 2);
        cart.add(manual(1, 1_000), 3);
        assertEquals(1, cart.getItems().size());
        assertEquals(5, cart.getItems().get(0).getQuantity());
    }

    @Test void sumOverFive_isConflict_andKeepsQuantity() {
        Cart cart = new Cart();
        cart.add(manual(1, 1_000), 3);
        assertThrows(ConflictException.class, () -> cart.add(manual(1, 1_000), 3));
        assertEquals(3, cart.getItems().get(0).getQuantity());
    }

    @Test void twentyFirstKind_isConflict() {
        Cart cart = new Cart();
        for (long i = 1; i <= 20; i++) cart.add(manual(i, 1_000), 1);
        assertThrows(ConflictException.class, () -> cart.add(manual(21, 1_000), 1));
        assertEquals(20, cart.getItems().size());
    }

    @Test void soldOutExternal_cannotBeAdded() {
        Cart cart = new Cart();
        assertThrows(ConflictException.class, () -> cart.add(external(1, 10_000, 0), 1));
        assertTrue(cart.getItems().isEmpty());
    }

    @Test void stockLimit_appliesToExternalOnly() {
        Cart cart = new Cart();
        assertThrows(ConflictException.class, () -> cart.add(external(1, 10_000, 3), 4));
        assertDoesNotThrow(() -> cart.add(manual(2, 10_000), 5));   // 직접 등록은 재고 제한 없음
    }

    @Test void failedQuantityChange_keepsItemQuantityAndCopiedValues() {
        Cart cart = new Cart();
        CartItem item = cart.add(external(1, 10_000, 3), 2);
        assertThrows(ConflictException.class, () -> cart.changeQuantity(item.getId(), 4));  // 재고 3 초과
        assertEquals(1, cart.getItems().size());
        assertEquals(2, item.getQuantity());
        assertEquals("외부1", item.getName());
        assertEquals(10_000, item.getPrice());
        assertEquals(3, item.getStock());
    }
}