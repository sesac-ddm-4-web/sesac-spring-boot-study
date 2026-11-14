package com.example.wishlist.infrastructure;

import com.example.wishlist.application.CartService;
import com.example.wishlist.application.WishService;
import com.example.wishlist.domain.CartConflictException;
import com.example.wishlist.domain.CartItem;
import com.example.wishlist.domain.CollectionItem;
import com.example.wishlist.domain.SourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseCartItemRepositoryTest {

    @Autowired
    private DatabaseCartItemRepository repository;

    @Autowired
    private DatabaseCollectionItemRepository wishRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private WishService wishService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearTables() {
        jdbcTemplate.execute("TRUNCATE TABLE cart_items");
        jdbcTemplate.execute("TRUNCATE TABLE collection_items");
    }

    private CollectionItem manualWish(String name, int price) {
        return wishRepository.add(new CollectionItem(name, price));
    }

    private CollectionItem externalWish(long externalId, int stock) {
        return wishRepository.add(new CollectionItem("Mascara", 9990, SourceType.DUMMYJSON, externalId, stock));
    }

    @Test
    @DisplayName("행 id, 원본 로컬 id, 수량과 처음 담은 이름·단가·재고 복사본을 그대로 복원")
    void roundTrip() {
        CollectionItem wish = externalWish(1L, 3);

        CartItem saved = repository.add(CartItem.copyOf(wish, 2));

        CartItem found = repository.findById(saved.getId());
        assertEquals(saved.getId(), found.getId());
        assertEquals(wish.getId(), found.getWishItemId());
        assertEquals(2, found.getQuantity());
        assertEquals("Mascara", found.getName());
        assertEquals(9990L, found.getUnitPrice());
        assertEquals(3, found.getStock());
        assertEquals(19980L, found.getLineTotal());
    }

    @Test
    @DisplayName("수동 상품 행의 재고는 NULL로 저장되고 null로 복원")
    void manualStockIsNull() {
        CollectionItem wish = manualWish("키보드", 50000);

        repository.add(CartItem.copyOf(wish, 1));

        assertNull(repository.findByWishItemId(wish.getId()).getStock());
    }

    @Test
    @DisplayName("목록은 ORDER BY id로 담은 순서")
    void findAllKeepsInsertOrder() {
        CollectionItem a = manualWish("A", 1000);
        CollectionItem b = manualWish("B", 1000);
        CartItem first = repository.add(CartItem.copyOf(b, 1));
        CartItem second = repository.add(CartItem.copyOf(a, 1));

        assertEquals(List.of(first.getId(), second.getId()),
                repository.findAll().stream().map(CartItem::getId).toList());
    }

    @Test
    @DisplayName("수량 합산, 수량 변경은 DB quantity에 반영")
    void quantityUpdate() {
        CollectionItem wish = manualWish("키보드", 50000);

        CartItem cartItem = cartService.add(wish.getId(), 2);
        cartService.add(wish.getId(), 1);
        assertEquals(3, repository.findById(cartItem.getId()).getQuantity());

        cartService.changeQuantity(cartItem.getId(), 5);
        assertEquals(5, repository.findById(cartItem.getId()).getQuantity());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    @DisplayName("수량 합산 초과, 재고 초과, 범위 밖 수량은 409/400이고 DB 수량 유지")
    void failedQuantityKeepsRow() {
        CollectionItem manual = manualWish("키보드", 50000);
        CollectionItem external = externalWish(1L, 3);
        CartItem manualRow = cartService.add(manual.getId(), 5);
        CartItem externalRow = cartService.add(external.getId(), 2);

        assertThrows(CartConflictException.class, () -> cartService.add(manual.getId(), 1));
        assertThrows(CartConflictException.class, () -> cartService.add(external.getId(), 2));
        assertThrows(CartConflictException.class, () -> cartService.changeQuantity(externalRow.getId(), 4));
        assertThrows(IllegalArgumentException.class, () -> cartService.changeQuantity(manualRow.getId(), 0));

        assertEquals(5, repository.findById(manualRow.getId()).getQuantity());
        assertEquals(2, repository.findById(externalRow.getId()).getQuantity());
    }

    @Test
    @DisplayName("같은 원본 id를 두 줄로 저장하면 DB UNIQUE로 거부, 기존 행 유지")
    void wishItemUniqueConstraint() {
        CollectionItem wish = manualWish("키보드", 50000);
        CartItem saved = repository.add(CartItem.copyOf(wish, 2));

        assertThrows(DuplicateKeyException.class, () -> repository.add(CartItem.copyOf(wish, 1)));

        List<CartItem> items = repository.findAll();
        assertEquals(1, items.size());
        assertEquals(saved.getId(), items.get(0).getId());
        assertEquals(2, items.get(0).getQuantity());
    }

    @Test
    @DisplayName("원본을 수정하거나 삭제해도 장바구니 행과 복사본은 그대로 (FK 없음)")
    void originalChangesDoNotTouchCart() {
        CollectionItem wish = manualWish("키보드", 50000);
        CartItem saved = cartService.add(wish.getId(), 2);

        wishService.update(wish.getId(), "새 키보드", 70000);
        wishService.delete(wish.getId());

        CartItem found = repository.findById(saved.getId());
        assertEquals(wish.getId(), found.getWishItemId());
        assertEquals("키보드", found.getName());
        assertEquals(50000L, found.getUnitPrice());

        cartService.changeQuantity(saved.getId(), 3);
        assertEquals(3, repository.findById(saved.getId()).getQuantity());
    }

    @Test
    @DisplayName("없는 행 조회는 null, 삭제는 한 번만 true")
    void notFoundAndDelete() {
        CartItem saved = repository.add(CartItem.copyOf(manualWish("키보드", 50000), 1));

        assertNull(repository.findById(999L));
        assertNull(repository.findByWishItemId(999L));
        assertTrue(repository.delete(saved.getId()));
        assertFalse(repository.delete(saved.getId()));
    }
}
