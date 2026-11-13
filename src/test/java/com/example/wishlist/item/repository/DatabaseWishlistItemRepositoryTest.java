package com.example.wishlist.item.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.wishlist.item.domain.SourceType;
import com.example.wishlist.item.domain.WishlistItem;
import com.example.wishlist.item.exception.DuplicateImportException;
import com.example.wishlist.support.DatabaseCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;


@SpringBootTest
class DatabaseWishlistItemRepositoryTest {

    @Autowired
    DatabaseWishlistItemRepository repository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearTable() {
        DatabaseCleaner.clear(jdbcTemplate);
    }

    @Test
    @DisplayName("저장하면 1부터 차례로 id가 증가한다 (AUTO_INCREMENT)")
    void saveSequentialId() {
        WishlistItem first = repository.save(WishlistItem.manual("지구 끝의 온실", 18000));
        WishlistItem second = repository.save(WishlistItem.manual("프로젝트 헤일메리", 30000));

        assertThat(first.getId()).isEqualTo(1L);
        assertThat(second.getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("저장해도 넘겨준 객체는 바뀌지 않고 id가 붙은 새 객체가 반환된다")
    void saveReturnsNewObject() {
        WishlistItem input = WishlistItem.manual("동물농장", 8000);
        WishlistItem saved = repository.save(input);

        assertThat(input.getId()).isNull();
        assertThat(saved).isNotSameAs(input);
        assertThat(saved.getName()).isEqualTo("동물농장");
        assertThat(saved.getPrice()).isEqualTo(8000);
    }

    @Test
    @DisplayName("저장하면 DB가 id를 부여하고, 모든 필드가 그대로 저장된다")
    void saveAndFind() {
        WishlistItem saved = repository.save(WishlistItem.imported(7L, "Essence Mascara", 9990, 3));

        WishlistItem found = repository.findById(saved.getId()).orElseThrow();

        assertThat(saved.getId()).isNotNull();
        assertThat(found.getName()).isEqualTo("Essence Mascara");
        assertThat(found.getPrice()).isEqualTo(9990);
        assertThat(found.getSourceType()).isEqualTo(SourceType.EXTERNAL);
        assertThat(found.getExternalId()).isEqualTo(7L);
        assertThat(found.getStock()).isEqualTo(3);
    }

    @Test
    @DisplayName("직접 등록 아이템은 externalId·stock이 NULL로 저장되고 null로 읽힌다")
    void saveManual() {
        WishlistItem saved = repository.save(WishlistItem.manual("데미안", 12000));

        WishlistItem found = repository.findById(saved.getId()).orElseThrow();

        assertThat(found.getSourceType()).isEqualTo(SourceType.MANUAL);
        assertThat(found.getExternalId()).isNull();
        assertThat(found.getStock()).isNull();
    }

    @Test
    @DisplayName("전체 목록은 등록 순서(id 오름차순)이고, 수정해도 순서는 그대로다")
    void findAllOrderAndUpdate() {
        WishlistItem first = repository.save(WishlistItem.manual("데미안", 12000));
        repository.save(WishlistItem.manual("어린 왕자", 5000));

        repository.update(first.changeTo("데미안 (개정판)", 13000));

        assertThat(repository.findAll()).extracting(WishlistItem::getName)
            .containsExactly("데미안 (개정판)", "어린 왕자");
        assertThat(repository.findById(first.getId()).orElseThrow().getPrice()).isEqualTo(13000);
    }

    @Test
    @DisplayName("삭제하면 true, 없는 id는 false")
    void delete() {
        WishlistItem saved = repository.save(WishlistItem.manual("데미안", 12000));

        assertThat(repository.deleteById(saved.getId())).isTrue();
        assertThat(repository.deleteById(saved.getId())).isFalse();
        assertThat(repository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("외부 상품 id로 찾을 수 있고, 같은 외부 상품은 DB 제약으로도 저장되지 않는다 (409)")
    void externalIdUnique() {
        repository.save(WishlistItem.imported(7L, "Essence Mascara", 9990, 3));

        assertThat(repository.findByExternalId(7L)).isPresent();
        assertThat(repository.findByExternalId(8L)).isEmpty();
        assertThatThrownBy(() -> repository.save(WishlistItem.imported(7L, "Essence Mascara", 9990, 3)))
            .isInstanceOf(DuplicateImportException.class);
    }
}