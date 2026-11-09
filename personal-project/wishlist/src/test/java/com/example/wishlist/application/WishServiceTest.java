package com.example.wishlist.application;

import com.example.wishlist.domain.CollectionItem;
import com.example.wishlist.infrastructure.ListCollectionItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WishServiceTest {

    private WishService service;

    @BeforeEach
    void setUp() {
        service = new WishService(new ListCollectionItemRepository());
    }

    private List<String> names(List<CollectionItem> items) {
        return items.stream().map(CollectionItem::getName).toList();
    }

    @Test
    @DisplayName("정상 등록: id가 부여되고 이름 앞뒤 공백이 제거된다")
    void register() {
        CollectionItem saved = service.register("  키보드 ", 50000);

        assertNotNull(saved.getId());
        assertEquals("키보드", saved.getName());
        assertEquals(50000, saved.getPrice());
    }

    @Test
    @DisplayName("빈 이름 거부: null, 빈 문자열, 공백만 있는 이름은 등록되지 않는다")
    void rejectBlankName() {
        service.register("키보드", 50000);

        assertThrows(IllegalArgumentException.class, () -> service.register(null, 1000));
        assertThrows(IllegalArgumentException.class, () -> service.register("", 1000));
        assertThrows(IllegalArgumentException.class, () -> service.register("   ", 1000));

        assertEquals(List.of("키보드"), names(service.findAll()));
    }

    @Test
    @DisplayName("음수 가격 거부: 실패한 등록 뒤 목록과 다음 id가 바뀌지 않는다")
    void rejectNegativePrice() {
        CollectionItem first = service.register("키보드", 50000);

        assertThrows(IllegalArgumentException.class, () -> service.register("마우스", -1));

        assertEquals(List.of("키보드"), names(service.findAll()));
        CollectionItem next = service.register("마우스", 30000);
        assertEquals(first.getId() + 1, next.getId());
    }

    @Test
    @DisplayName("음수 예산 거부")
    void rejectNegativeBudget() {
        service.register("키보드", 50000);

        assertThrows(IllegalArgumentException.class, () -> service.recommend(-1));
    }

    @Test
    @DisplayName("예산 경계: 가격이 예산과 같으면 포함, 1원이라도 넘으면 제외")
    void budgetBoundary() {
        service.register("책", 10000);

        assertEquals(List.of("책"), names(service.recommend(10000)));
        assertTrue(service.recommend(9999).isEmpty());
    }
}
