package com.example.wishlist.item.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.wishlist.item.domain.WishlistItem;
import com.example.wishlist.item.exception.ImportedItemNotEditableException;
import com.example.wishlist.item.exception.InvalidItemException;
import com.example.wishlist.item.exception.ItemNotFoundException;
import com.example.wishlist.item.repository.WishlistItemRepository;
import com.example.wishlist.support.DatabaseCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class WishlistServiceEditTest {

    @Autowired
    private WishlistItemRepository repository;

    @Autowired
    private WishlistService service;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        DatabaseCleaner.clear(jdbcTemplate);
    }

    @Test
    @DisplayName("직접 등록한 아이템은 이름·가격을 수정할 수 있고, 등록 순서는 그대로다")
    void updateManualItem() {
        WishlistItem first = service.register("데미안", 12000);
        service.register("동물농장", 8000);

        WishlistItem updated = service.update(first.getId(), "  데미안 (개정판) ", 13000);

        assertThat(updated.getName()).isEqualTo("데미안 (개정판)");
        assertThat(updated.getPrice()).isEqualTo(13000);
        assertThat(service.findAll()).extracting(WishlistItem::getName)
            .containsExactly("데미안 (개정판)", "동물농장");
    }

    @Test
    @DisplayName("수정 값이 규칙에 맞지 않으면 400이고 기존 값은 그대로다")
    void invalidUpdateKeepsItem() {
        WishlistItem saved = service.register("데미안", 12000);

        assertThatThrownBy(() -> service.update(saved.getId(), " ", 13000))
            .isInstanceOf(InvalidItemException.class);
        assertThatThrownBy(() -> service.update(saved.getId(), "데미안", -1))
            .isInstanceOf(InvalidItemException.class);

        assertThat(service.findById(saved.getId()).getName()).isEqualTo("데미안");
        assertThat(service.findById(saved.getId()).getPrice()).isEqualTo(12000);
    }

    @Test
    @DisplayName("외부에서 가져온 아이템은 이름·가격을 수정할 수 없고 값도 그대로다")
    void importedItemIsNotEditable() {
        WishlistItem imported = repository.save(WishlistItem.imported(1L, "Essence Mascara", 9990, 3));

        assertThatThrownBy(() -> service.update(imported.getId(), "다른 이름", 1))
            .isInstanceOf(ImportedItemNotEditableException.class);

        WishlistItem after = service.findById(imported.getId());
        assertThat(after.getName()).isEqualTo("Essence Mascara");
        assertThat(after.getPrice()).isEqualTo(9990);
    }

    @Test
    @DisplayName("삭제하면 목록에서 빠지고, 없는 id를 수정·삭제하면 404다")
    void delete() {
        WishlistItem saved = service.register("데미안", 12000);

        service.delete(saved.getId());

        assertThat(service.findAll()).isEmpty();
        assertThatThrownBy(() -> service.delete(saved.getId()))
            .isInstanceOf(ItemNotFoundException.class);
        assertThatThrownBy(() -> service.update(saved.getId(), "x", 1))
            .isInstanceOf(ItemNotFoundException.class);
    }
}