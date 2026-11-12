package com.example.hobby_wishlist.domain;

import com.example.hobby_wishlist.domain.cart.Cart;
import com.example.hobby_wishlist.domain.cart.CartItemNotFoundException;
import com.example.hobby_wishlist.domain.cart.CartRuleViolationException;
import com.example.hobby_wishlist.domain.wishlist.WishlistItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

public class CartTests {
    private Cart cart;

    // 수동 상품: 재고 제한 없음 / 외부 상품: 재고 3
    private final WishlistItem manualItem = WishlistItem.fromManual(1L, "의자", 25000);
    private final WishlistItem externalItem = WishlistItem.fromExternal(2L, 55L, "폰", 9990, 3);
    private final WishlistItem soldOutItem = WishlistItem.fromExternal(3L, 100L, "품절 항목", 5000, 0);

    @BeforeEach
    void setUp() {
        cart = new Cart();
    }

    @Test
    @DisplayName("같은 상품을 다시 담으면 줄을 늘리지 않고 수량을 합산함")
    void addSameProductTest() {
        cart.add(manualItem, 1);
        cart.add(manualItem, 2);

        assertEquals(1, cart.getItems().size());
        assertEquals(3, cart.getItems().getFirst().getQuantity());
    }

    @Test
    @DisplayName("수량이 5를 넘는 항목 추가를 거부하고 장바구니에 추가하지 않음")
    void addItemInvalidQuantityTest() {
        assertThrows(CartRuleViolationException.class, () -> cart.add(manualItem, 6));
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    @DisplayName("합산 수량이 5를 넘으면 거부하고 기존 수량은 유지함")
    void mergeItemQuantityOverflowTest() {
        assertDoesNotThrow(() -> cart.add(manualItem, 5));
        assertThrows(CartRuleViolationException.class, () -> cart.add(manualItem, 1));
        assertEquals(5, cart.getItems().getFirst().getQuantity());
    }

    @Test
    @DisplayName("재고 수량을 넘는 수량의 항목 추가를 거부하고 장바구니에 추가하지 않음")
    void addItemQuantityOverStockTest() {
        assertThrows(CartRuleViolationException.class, () -> cart.add(externalItem, 4));
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    @DisplayName("합산 수량이 재고 수량를 넘으면 거부하고 기존 수량은 유지함")
    void mergeItemQuantityOverStockTest() {
        assertDoesNotThrow(() -> cart.add(externalItem, 3));
        assertThrows(CartRuleViolationException.class, () -> cart.add(externalItem, 2));
        assertEquals(3, cart.getItems().getFirst().getQuantity());
    }

    @Test
    @DisplayName("재고가 없는 상품은 장바구니에 담을 수 없음")
    void addSoldOutTest() {
        assertThrows(CartRuleViolationException.class, () -> cart.add(soldOutItem, 1));
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    @DisplayName("수량 변경 테스트")
    void changeQuantityTest() {
        cart.add(externalItem, 1);
        cart.assignIds(new AtomicLong(0L)::getAndIncrement);
        Long id = cart.getItems().getFirst().getId();

        // 변경 성공 시
        cart.changeQuantity(id, 3);
        assertEquals(3, cart.getItems().getFirst().getQuantity());

        // 변경 실패 시
        assertThrows(CartRuleViolationException.class, () -> cart.changeQuantity(id, 4));
        assertEquals(3, cart.getItems().getFirst().getQuantity());
    }

    @Test
    @DisplayName("없는 장바구니 항목 변경·삭제는 CartItemNotFoundException")
    void notFoundTest() {
        assertThrows(CartItemNotFoundException.class, () -> cart.changeQuantity(99L, 1));
        assertThrows(CartItemNotFoundException.class, () -> cart.remove(999L));
    }

    @Test
    @DisplayName("삭제하면 동일 상품을 다시 담을 수 있고 장바구니 id는 달라짐")
    void addItemAfterRemoveTest() {
        cart.add(manualItem, 1);
        cart.assignIds(new AtomicLong(0L)::getAndIncrement);

        // 삭제 후 장바구니 비어 있음
        Long cartId = cart.getItems().getFirst().getId();
        cart.remove(cartId);
        assertTrue(cart.getItems().isEmpty());

        // 다시 같은 상품 추가
        cart.add(manualItem, 1);
        assertNotEquals(cartId, cart.getItems().getFirst().getId());
    }

    @Test
    @DisplayName("삭제하면 동일 상품을 다시 담을 수 있고 장바구니 id는 달라짐")
    void addUpdatedItemAfterRemoveTest(){
        cart.add(manualItem, 1);
        cart.assignIds(new AtomicLong(0L)::getAndIncrement);

        // 삭제 후 장바구니 비어 있음
        Long cartId = cart.getItems().getFirst().getId();
        cart.remove(cartId);
        assertTrue(cart.getItems().isEmpty());

        // 상품 id가 동일한데 가격 정보가 업데이트됨
        WishlistItem updatedItem = WishlistItem.fromManual(1L, "의자", 30000);
        cart.add(updatedItem, 1);
        assertEquals(30000, cart.getItems().getFirst().getPrice());
    }
}
