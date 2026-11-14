package com.example.wishlist.application;

import com.example.wishlist.domain.CollectionItem;
import com.example.wishlist.domain.DuplicateItemException;
import com.example.wishlist.domain.ExternalProduct;
import com.example.wishlist.domain.SourceType;
import com.example.wishlist.infrastructure.FakeDummyJsonClient;
import com.example.wishlist.infrastructure.ListCollectionItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalProductServiceTest {

    private FakeDummyJsonClient client;
    private ExternalProductService service;
    private WishService wishService;

    @BeforeEach
    void setUp() {
        client = new FakeDummyJsonClient();
        client.put(1, "Essence Mascara", "9.99", 5);
        client.put(2, "Sold Out Lipstick", "12.5", 0);
        ListCollectionItemRepository repository = new ListCollectionItemRepository();
        service = new ExternalProductService(client, repository);
        wishService = new WishService(repository);
    }

    @Test
    @DisplayName("검색 결과가 없으면 빈 목록, 가격은 원화로 환산")
    void search() {
        assertTrue(service.search("없는상품", 10, 0).isEmpty());

        ExternalProduct found = service.search("Mascara", 10, 0).get(0);
        assertEquals(9990, found.price());
    }

    @Test
    @DisplayName("가져오기: 서버가 조회한 이름, 가격(원)으로 저장하고, 내 id와 외부 id를 따로 둔다")
    void importProduct() {
        wishService.register("수동 상품", 1000);
        CollectionItem saved = service.importToWishlist(1L);

        assertEquals(2L, saved.getId());
        assertEquals(1L, saved.getExternalId());
        assertEquals(SourceType.DUMMYJSON, saved.getSourceType());
        assertEquals("Essence Mascara", saved.getName());
        assertEquals(9990, saved.getPrice());
        assertEquals(5, saved.getStock());
    }

    @Test
    @DisplayName("같은 외부 상품을 다시 가져오면 409(DuplicateItemException), 목록은 그대로")
    void rejectDuplicate() {
        service.importToWishlist(1L);

        assertThrows(DuplicateItemException.class, () -> service.importToWishlist(1L));
        assertEquals(1, wishService.findAll().size());
    }

    @Test
    @DisplayName("품절 상품도 위시리스트에는 저장된다")
    void importSoldOut() {
        CollectionItem saved = service.importToWishlist(2L);
        assertEquals(0, saved.getStock());
    }

    @Test
    @DisplayName("없는 외부 상품은 404, 외부 서버 장애는 502. 둘 다 저장되지 않는다")
    void importFailureKeepsData() {
        ResponseStatusException notFound = assertThrows(ResponseStatusException.class,
                () -> service.importToWishlist(999L));
        assertEquals(404, notFound.getStatusCode().value());

        client.down = true;
        ResponseStatusException badGateway = assertThrows(ResponseStatusException.class,
                () -> service.importToWishlist(1L));
        assertEquals(502, badGateway.getStatusCode().value());

        assertTrue(wishService.findAll().isEmpty());
    }

    @Test
    @DisplayName("외부 상품은 수정 거부, 수동 상품은 수정 가능")
    void updateRules() {
        CollectionItem external = service.importToWishlist(1L);
        CollectionItem manual = wishService.register("키보드", 50000);

        assertThrows(IllegalArgumentException.class,
                () -> wishService.update(external.getId(), "바꾼 이름", 1));
        assertEquals("Essence Mascara", external.getName());
        assertEquals(9990, external.getPrice());

        wishService.update(manual.getId(), " 무선 키보드 ", 60000);
        assertEquals("무선 키보드", manual.getName());
        assertEquals(60000, manual.getPrice());
    }
}
