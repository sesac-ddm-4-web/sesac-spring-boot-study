package com.example.wishlist.item.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.wishlist.item.domain.SourceType;
import com.example.wishlist.item.domain.WishlistItem;
import com.example.wishlist.item.exception.DuplicateImportException;
import com.example.wishlist.item.repository.WishlistItemRepository;
import com.example.wishlist.product.exception.ExternalApiException;
import com.example.wishlist.product.exception.ExternalProductNotFoundException;
import com.example.wishlist.support.DatabaseCleaner;
import com.example.wishlist.support.FakeProductClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class WishlistImportServiceTest {

    @Autowired
    private WishlistItemRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private FakeProductClient client;
    private WishlistImportService importService;

    @BeforeEach
    void setUp() {
        DatabaseCleaner.clear(jdbcTemplate);
        client = new FakeProductClient()
            .add(1L, "Essence Mascara Lash Princess", "9.99", 3)
            .add(2L, "Sold Out Book", "12.5", 0);
        importService = new WishlistImportService(repository, client);
    }

    @Test
    @DisplayName("외부 상품 id만으로 가져오고, 이름·가격·재고는 서버가 상세 API에서 읽는다")
    void importByExternalIdOnly() {
        WishlistItem saved = importService.importProduct(1L);

        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getName()).isEqualTo("Essence Mascara Lash Princess");
        assertThat(saved.getSourceType()).isEqualTo(SourceType.EXTERNAL);
        assertThat(saved.getExternalId()).isEqualTo(1L);
        assertThat(saved.getStock()).isEqualTo(3);
    }

    @Test
    @DisplayName("가격은 외부 1단위 = 1,000원으로 환산한다 (9.99 → 9,990원)")
    void convertsPrice() {
        assertThat(importService.importProduct(1L).getPrice()).isEqualTo(9990);
        assertThat(importService.importProduct(2L).getPrice()).isEqualTo(12500);
    }

    @Test
    @DisplayName("내 위시리스트 id와 외부 상품 id는 따로 관리된다")
    void separateIds() {
        repository.save(new WishlistItem(null, "직접 등록한 책", 15000));   // 내 id 1

        WishlistItem imported = importService.importProduct(1L);         // 외부 id 1

        assertThat(imported.getId()).isEqualTo(2L);                     // 내 위시리스트 id는 2
        assertThat(imported.getExternalId()).isEqualTo(1L);             // 외부 id는 1 그대로
        WishlistItem found = repository.findByExternalId(1L).orElseThrow();
        assertThat(found.getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("같은 외부 상품을 다시 가져오면 409, 외부 호출 없이 목록도 그대로")
    void duplicateImport() {
        importService.importProduct(1L);

        assertThatThrownBy(() -> importService.importProduct(1L))
            .isInstanceOf(DuplicateImportException.class);
        assertThat(client.findByIdCalls()).isEqualTo(1);
        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("품절 상품(재고 0)도 위시리스트에는 저장할 수 있다")
    void soldOutCanBeImported() {
        WishlistItem saved = importService.importProduct(2L);

        assertThat(saved.isSoldOut()).isTrue();
        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("외부에 없는 상품이면 404, 외부 서버 장애면 502이고 둘 다 저장하지 않는다")
    void failuresDoNotSave() {
        assertThatThrownBy(() -> importService.importProduct(999L))
            .isInstanceOf(ExternalProductNotFoundException.class);

        client.failAll();
        assertThatThrownBy(() -> importService.importProduct(1L))
            .isInstanceOf(ExternalApiException.class);

        assertThat(repository.findAll()).isEmpty();
    }
}