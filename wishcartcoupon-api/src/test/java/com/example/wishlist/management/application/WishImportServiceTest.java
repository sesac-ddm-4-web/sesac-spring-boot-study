package com.example.wishlist.management.application;

import com.example.wishlist.management.application.external.WishImportService;
import com.example.wishlist.management.domain.external.ExternalProduct;
import com.example.wishlist.management.domain.external.ExternalProductClient;
import com.example.wishlist.management.domain.wish.Wish;
import com.example.wishlist.management.domain.wish.WishRepository;
import com.example.wishlist.management.domain.exception.ConflictException;
import com.example.wishlist.management.domain.exception.ExternalApiException;
import com.example.wishlist.management.infrastructure.wish.ListWishRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// 가져오기 테스트: 외부 Client는 Mockito 목, 저장소는 진짜 사용
class WishImportServiceTest {
    ExternalProductClient client = mock(ExternalProductClient.class);
    WishRepository repository = new ListWishRepository();
    WishImportService service = new WishImportService(client, repository);

    private ExternalProduct product(int stock) {
        return new ExternalProduct(5, "Mascara", new BigDecimal("9.99"), "beauty", "t.png", stock);
    }

    @Test
    void importsWithConvertedPrice() {
        when(client.get(5L)).thenReturn(product(30));
        Wish wish = service.importWish(5);
        assertEquals(9_990, wish.getPrice());
        assertEquals(1, repository.getWishList().size());
    }

    @Test
    void allowsSoldOutProduct() {
        when(client.get(5L)).thenReturn(product(0));
        assertDoesNotThrow(() -> service.importWish(5));
    }

    @Test
    void rejectsDuplicate() {
        when(client.get(5L)).thenReturn(product(30));
        service.importWish(5);
        assertThrows(ConflictException.class, () -> service.importWish(5));
        assertEquals(1, repository.getWishList().size());
    }

    @Test
    void keepsRepositoryWhenExternalFails() {
        when(client.get(5L)).thenThrow(new ExternalApiException("fail", null));
        assertThrows(ExternalApiException.class, () -> service.importWish(5));
        assertTrue(repository.getWishList().isEmpty());   // 실패 시 저장 데이터 보존
    }
}