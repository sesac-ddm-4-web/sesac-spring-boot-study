package com.example.wishlist.application;

import com.example.wishlist.domain.CartConflictException;
import com.example.wishlist.domain.CartItem;
import com.example.wishlist.domain.CartItemNotFoundException;
import com.example.wishlist.domain.CollectionItem;
import com.example.wishlist.domain.ItemNotFoundException;
import com.example.wishlist.domain.PaymentPreview;
import com.example.wishlist.infrastructure.FakeDummyJsonClient;
import com.example.wishlist.infrastructure.ListCartItemRepository;
import com.example.wishlist.infrastructure.ListCollectionItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartServiceTest {

    private WishService wishService;
    private ExternalProductService externalService;
    private CartService cartService;

    @BeforeEach
    void setUp() {
        ListCollectionItemRepository wishRepository = new ListCollectionItemRepository();
        FakeDummyJsonClient client = new FakeDummyJsonClient();
        client.put(1, "재고 3개 상품", "9.99", 3);
        client.put(2, "품절 상품", "5", 0);

        wishService = new WishService(wishRepository);
        externalService = new ExternalProductService(client, wishRepository);
        cartService = new CartService(new ListCartItemRepository(), wishRepository);
    }

    private int quantityOfFirst() {
        return cartService.findCart().getItems().get(0).getQuantity();
    }

    @Test
    @DisplayName("담기: 이름, 단가를 복사하고 합계는 단가 × 수량")
    void add() {
        CollectionItem keyboard = wishService.register("키보드", 50000);

        CartItem cartItem = cartService.add(keyboard.getId(), 2);

        assertEquals("키보드", cartItem.getName());
        assertEquals(50000L, cartItem.getUnitPrice());
        assertEquals(100000L, cartItem.getLineTotal());
        assertEquals(100000L, cartService.findCart().getSubtotal());
        assertEquals(2, cartService.findCart().getTotalQuantity());
    }

    @Test
    @DisplayName("같은 상품을 다시 담으면 한 줄에 합산, 합산 후 5개 초과는 409이고 수량 유지")
    void mergeSameItem() {
        CollectionItem keyboard = wishService.register("키보드", 50000);
        cartService.add(keyboard.getId(), 3);
        cartService.add(keyboard.getId(), 2);

        assertEquals(1, cartService.findCart().getItems().size());
        assertEquals(5, quantityOfFirst());

        assertThrows(CartConflictException.class, () -> cartService.add(keyboard.getId(), 1));
        assertEquals(5, quantityOfFirst());
    }

    @Test
    @DisplayName("외부 상품은 저장된 stock 이하만(초과 409), 품절 상품은 담을 수 없음(409)")
    void externalStock() {
        CollectionItem limited = externalService.importToWishlist(1L);
        CollectionItem soldOut = externalService.importToWishlist(2L);

        assertThrows(CartConflictException.class, () -> cartService.add(limited.getId(), 4));
        cartService.add(limited.getId(), 3);
        assertThrows(CartConflictException.class, () -> cartService.add(limited.getId(), 1));
        assertEquals(3, quantityOfFirst());

        assertThrows(CartConflictException.class, () -> cartService.add(soldOut.getId(), 1));
        assertEquals(1, cartService.findCart().getItems().size());
    }

    @Test
    @DisplayName("수동 상품은 재고 제한 없이 5개까지 담을 수 있다")
    void manualHasNoStockLimit() {
        CollectionItem keyboard = wishService.register("키보드", 50000);
        cartService.add(keyboard.getId(), 5);
        assertEquals(5, quantityOfFirst());
    }

    @Test
    @DisplayName("상품 종류는 최대 20종, 21번째는 409")
    void maxKinds() {
        for (int i = 1; i <= 20; i++) {
            CollectionItem item = wishService.register("상품" + i, 1000);
            cartService.add(item.getId(), 1);
        }
        CollectionItem extra = wishService.register("21번째", 1000);

        assertThrows(CartConflictException.class, () -> cartService.add(extra.getId(), 1));
        assertEquals(20, cartService.findCart().getItems().size());
    }

    @Test
    @DisplayName("없는 위시리스트 상품은 담을 수 없음 (404)")
    void addNotFound() {
        assertThrows(ItemNotFoundException.class, () -> cartService.add(99L, 1));
    }

    @Test
    @DisplayName("원본을 수정해도 장바구니는 처음 복사한 값 유지, 중복 추가도 처음 단가 사용")
    void keepCopiedValues() {
        CollectionItem keyboard = wishService.register("키보드", 50000);
        cartService.add(keyboard.getId(), 1);

        wishService.update(keyboard.getId(), "새 키보드", 70000);
        cartService.add(keyboard.getId(), 1);

        CartItem cartItem = cartService.findCart().getItems().get(0);
        assertEquals("키보드", cartItem.getName());
        assertEquals(50000L, cartItem.getUnitPrice());
        assertEquals(100000L, cartService.findCart().getSubtotal());
    }

    @Test
    @DisplayName("원본을 삭제해도 장바구니 행 id로 수량 변경, 삭제 가능")
    void originalDeleted() {
        CollectionItem keyboard = wishService.register("키보드", 50000);
        CartItem cartItem = cartService.add(keyboard.getId(), 1);

        wishService.delete(keyboard.getId());

        assertEquals("키보드", cartService.findCart().getItems().get(0).getName());
        cartService.changeQuantity(cartItem.getId(), 4);
        assertEquals(200000L, cartService.findCart().getSubtotal());
        cartService.remove(cartItem.getId());
        assertTrue(cartService.findCart().getItems().isEmpty());
    }

    @Test
    @DisplayName("삭제 후 다시 담으면 현재 위시리스트 값을 새로 복사")
    void readdCopiesCurrentValue() {
        CollectionItem keyboard = wishService.register("키보드", 50000);
        CartItem first = cartService.add(keyboard.getId(), 1);
        wishService.update(keyboard.getId(), "새 키보드", 70000);

        cartService.remove(first.getId());
        CartItem second = cartService.add(keyboard.getId(), 1);

        assertEquals("새 키보드", second.getName());
        assertEquals(70000L, second.getUnitPrice());
    }

    @Test
    @DisplayName("잘못된 수량 변경은 기존 수량을 그대로 둔다")
    void invalidChangeKeepsState() {
        CollectionItem limited = externalService.importToWishlist(1L);
        CartItem cartItem = cartService.add(limited.getId(), 2);

        assertThrows(IllegalArgumentException.class, () -> cartService.changeQuantity(cartItem.getId(), 0));
        assertThrows(IllegalArgumentException.class, () -> cartService.changeQuantity(cartItem.getId(), 6));
        assertThrows(CartConflictException.class, () -> cartService.changeQuantity(cartItem.getId(), 4));
        assertThrows(CartItemNotFoundException.class, () -> cartService.changeQuantity(99L, 1));
        assertThrows(CartItemNotFoundException.class, () -> cartService.remove(99L));

        assertEquals(2, quantityOfFirst());
        assertEquals(1, cartService.findCart().getItems().size());
    }

    @Test
    @DisplayName("미리보기: 계산만 하고 장바구니는 바뀌지 않는다 (실패해도 마찬가지)")
    void previewDoesNotChangeCart() {
        CollectionItem item = wishService.register("모니터", 55000);
        cartService.add(item.getId(), 2);

        PaymentPreview preview = cartService.preview("PERCENT10");
        assertEquals(104_000, preview.finalAmount());

        assertThrows(IllegalArgumentException.class, () -> cartService.preview("WRONG"));
        assertEquals(2, quantityOfFirst());
        assertEquals(110_000, cartService.findCart().getSubtotal());
    }

    @Test
    @DisplayName("빈 장바구니 미리보기: NONE은 모두 0원, 다른 쿠폰은 거부")
    void emptyCartPreview() {
        assertEquals(0, cartService.preview(null).finalAmount());
        assertThrows(IllegalArgumentException.class, () -> cartService.preview("PERCENT10"));
    }
}
