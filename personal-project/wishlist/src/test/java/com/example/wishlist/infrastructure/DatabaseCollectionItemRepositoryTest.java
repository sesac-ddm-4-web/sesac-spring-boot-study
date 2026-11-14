package com.example.wishlist.infrastructure;

import com.example.wishlist.application.WishService;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseCollectionItemRepositoryTest {

    @Autowired
    private DatabaseCollectionItemRepository repository;

    @Autowired
    private WishService wishService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearTables() {
        jdbcTemplate.execute("TRUNCATE TABLE cart_items");
        jdbcTemplate.execute("TRUNCATE TABLE collection_items");
    }

    @Test
    @DisplayName("수동 상품: DB가 id를 부여하고, source는 MANUAL 문자열, externalId·stock은 NULL로 저장 후 그대로 복원")
    void manualItemRoundTrip() {
        CollectionItem saved = repository.add(new CollectionItem("키보드", 50000));
        assertNotNull(saved.getId());

        String storedSource = jdbcTemplate.queryForObject(
                "SELECT source_type FROM collection_items WHERE id = ?", String.class, saved.getId());
        assertEquals("MANUAL", storedSource);

        CollectionItem found = repository.findById(saved.getId());
        assertEquals("키보드", found.getName());
        assertEquals(50000, found.getPrice());
        assertEquals(SourceType.MANUAL, found.getSourceType());
        assertNull(found.getExternalId());
        assertNull(found.getStock());
    }

    @Test
    @DisplayName("외부 상품: source, externalId, stock을 복원하고 source·externalId 조합으로 찾는다")
    void externalItemRoundTrip() {
        CollectionItem saved = repository.add(new CollectionItem("Mascara", 9990, SourceType.DUMMYJSON, 1L, 3));

        CollectionItem found = repository.findByExternalId(SourceType.DUMMYJSON, 1L);
        assertEquals(saved.getId(), found.getId());
        assertEquals("Mascara", found.getName());
        assertEquals(SourceType.DUMMYJSON, found.getSourceType());
        assertEquals(1L, found.getExternalId());
        assertEquals(3, found.getStock());
        assertTrue(found.isExternal());
    }

    @Test
    @DisplayName("목록은 ORDER BY id로 등록 순서, 가격이 같은 추천도 등록 순서 유지")
    void findAllKeepsInsertOrder() {
        CollectionItem first = repository.add(new CollectionItem("세 번째로 싼 상품", 3000));
        CollectionItem second = repository.add(new CollectionItem("동률 A", 1000));
        CollectionItem third = repository.add(new CollectionItem("동률 B", 1000));

        List<CollectionItem> items = repository.findAll();
        assertEquals(List.of(first.getId(), second.getId(), third.getId()),
                items.stream().map(CollectionItem::getId).toList());

        List<CollectionItem> recommended = wishService.recommend(5000);
        assertEquals(List.of("동률 A", "동률 B", "세 번째로 싼 상품"),
                recommended.stream().map(CollectionItem::getName).toList());
    }

    @Test
    @DisplayName("수정: 이름, 가격만 DB에 반영되고 외부 정보는 그대로")
    void update() {
        CollectionItem saved = repository.add(new CollectionItem("마우스", 20000));

        wishService.update(saved.getId(), "무선 마우스", 25000);

        CollectionItem found = repository.findById(saved.getId());
        assertEquals("무선 마우스", found.getName());
        assertEquals(25000, found.getPrice());
        assertEquals(SourceType.MANUAL, found.getSourceType());
    }

    @Test
    @DisplayName("외부 상품 수정 거부, 음수 가격 수정 거부: 실패 후 DB 값 유지")
    void failedUpdateKeepsRow() {
        CollectionItem external = repository.add(new CollectionItem("Mascara", 9990, SourceType.DUMMYJSON, 1L, 3));
        CollectionItem manual = repository.add(new CollectionItem("마우스", 20000));

        assertThrows(IllegalArgumentException.class,
                () -> wishService.update(external.getId(), "바뀐 이름", 1));
        assertThrows(IllegalArgumentException.class,
                () -> wishService.update(manual.getId(), "마우스", -1));

        assertEquals("Mascara", repository.findById(external.getId()).getName());
        assertEquals(9990, repository.findById(external.getId()).getPrice());
        assertEquals(20000, repository.findById(manual.getId()).getPrice());
    }

    @Test
    @DisplayName("수동 상품은 같은 이름도 각각 저장된다")
    void manualNameDuplicateAllowed() {
        repository.add(new CollectionItem("키보드", 50000));
        repository.add(new CollectionItem("키보드", 50000));

        assertEquals(2, repository.findAll().size());
    }

    @Test
    @DisplayName("같은 외부 상품(source + externalId)은 DB UNIQUE로 거부, 기존 행 유지")
    void externalUniqueConstraint() {
        CollectionItem saved = repository.add(new CollectionItem("Mascara", 9990, SourceType.DUMMYJSON, 1L, 3));

        assertThrows(DuplicateKeyException.class,
                () -> repository.add(new CollectionItem("다른 이름", 1, SourceType.DUMMYJSON, 1L, 0)));

        List<CollectionItem> items = repository.findAll();
        assertEquals(1, items.size());
        assertEquals(saved.getId(), items.get(0).getId());
        assertEquals("Mascara", items.get(0).getName());
        assertEquals(3, items.get(0).getStock());
    }

    @Test
    @DisplayName("없는 id 조회는 null, 없는 외부 상품 조회는 null, 삭제는 한 번만 true")
    void notFoundAndDelete() {
        CollectionItem saved = repository.add(new CollectionItem("키보드", 50000));

        assertNull(repository.findById(999L));
        assertNull(repository.findByExternalId(SourceType.DUMMYJSON, 999L));
        assertTrue(repository.delete(saved.getId()));
        assertFalse(repository.delete(saved.getId()));
        assertNull(repository.findById(saved.getId()));
    }
}
