package com.example.wishlistbudget.service;

import com.example.wishlistbudget.domain.CollectionItem;
import com.example.wishlistbudget.domain.SourceType;
import com.example.wishlistbudget.exception.CollectionItemNotFoundException;
import com.example.wishlistbudget.exception.DuplicateExternalProductException;
import com.example.wishlistbudget.external.client.DummyJsonClient;
import com.example.wishlistbudget.external.dto.DummyJsonProduct;
import com.example.wishlistbudget.repository.CollectionRepository;
import com.example.wishlistbudget.repository.MemoryCollectionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CollectionServiceTest {

    private CollectionService service;
    private CollectionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MemoryCollectionRepository();

        DummyJsonClient client = new DummyJsonClient(RestClient.create()) {

            @Override
            public DummyJsonProduct getProduct(Long id) {

                if (id.equals(1L)) {
                    return new DummyJsonProduct(
                            1L, "운동화",
                            new BigDecimal("50.00"),
                            "shoes", "thumbnail.png", 10
                    );
                }

                if (id.equals(2L)) {
                    return new DummyJsonProduct(
                            2L, "품절 운동화",
                            new BigDecimal("30.00"),
                            "shoes", "thumbnail.png", 0
                    );
                }

                throw new IllegalArgumentException("존재하지 않는 상품");
            }
        };

        service = new CollectionService(repository, client);
    }

    @Test
    @DisplayName("위시리스트에 상품을 정상 등록한다")
    void addItem_success() {

        CollectionItem result = service.addItem("운동화", 50000);

        assertEquals("운동화", result.name());
        assertEquals(50000, result.price());
        assertEquals(SourceType.MANUAL, result.sourceType());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    @DisplayName("상품 이름이 공백이면 등록할 수 없다")
    void addItem_blankName() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.addItem("   ", 50000)
        );

        assertEquals(0, repository.findAll().size());
    }

    @Test
    @DisplayName("상품 가격이 음수이면 등록할 수 없다")
    void addItem_negativePrice() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.addItem("운동화", -1000)
        );

        assertEquals(0, repository.findAll().size());
    }

    @Test
    @DisplayName("위시리스트의 모든 상품을 조회한다")
    void getAllItems_success() {

        service.addItem("운동화", 50000);
        service.addItem("운동복", 30000);

        List<CollectionItem> result = service.getAllItems();

        assertEquals(2, result.size());
        assertEquals("운동화", result.get(0).name());
        assertEquals("운동복", result.get(1).name());
    }

    @Test
    @DisplayName("존재하지 않는 상품을 조회하면 예외가 발생한다")
    void getItem_notFound() {

        assertThrows(
                CollectionItemNotFoundException.class,
                () -> service.getItem(999L)
        );
    }

    @Test
    @DisplayName("예산 이하의 상품을 가격순으로 최대 3개 추천한다")
    void recommend_success() {

        service.addItem("운동화", 50000);
        service.addItem("양말", 5000);
        service.addItem("운동복", 30000);
        service.addItem("물통", 10000);
        service.addItem("고가 운동화", 100000);

        List<CollectionItem> result = service.recommend(50000);

        assertEquals(3, result.size());
        assertEquals("양말", result.get(0).name());
        assertEquals("물통", result.get(1).name());
        assertEquals("운동복", result.get(2).name());
    }

    @Test
    @DisplayName("외부 상품을 위시리스트에 정상 등록한다")
    void importProduct_success() {

        CollectionItem result = service.importProduct(1L);

        assertEquals("운동화", result.name());
        assertEquals(50000, result.price());
        assertEquals(SourceType.EXTERNAL, result.sourceType());
        assertEquals(1L, result.externalId());
        assertEquals(10, result.stock());
    }

    @Test
    @DisplayName("동일한 외부 상품은 중복 등록할 수 없다")
    void importProduct_duplicate() {

        service.importProduct(1L);

        assertThrows(
                DuplicateExternalProductException.class,
                () -> service.importProduct(1L)
        );

        assertEquals(1, repository.findAll().size());
    }

    @Test
    @DisplayName("재고가 0인 외부 상품도 위시리스트에 등록할 수 있다")
    void importProduct_outOfStock() {

        CollectionItem result = service.importProduct(2L);

        assertEquals(0, result.stock());
        assertEquals(30000, result.price());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    @DisplayName("외부 상품 ID가 0이면 등록할 수 없다")
    void importProduct_invalidId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.importProduct(0L)
        );

        assertEquals(0, repository.findAll().size());
    }
}