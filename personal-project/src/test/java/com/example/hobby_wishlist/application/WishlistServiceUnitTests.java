package com.example.hobby_wishlist.application;

import com.example.hobby_wishlist.domain.externalproduct.ExternalProduct;
import com.example.hobby_wishlist.domain.externalproduct.ExternalProductProvider;
import com.example.hobby_wishlist.domain.wishlist.*;
import com.example.hobby_wishlist.dto.wishlist.CreateWishlistItemRequest;
import com.example.hobby_wishlist.dto.wishlist.WishlistItemResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WishlistServiceUnitTests {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private ExternalProductProvider externalProductProvider;

    @InjectMocks
    private WishlistService wishlistService;

    @Test
    @DisplayName("등록 시 이름의 앞뒤 공백은 제거하고 Repository가 저장한 결과를 DTO로 변환해 반환함")
    void stripNameAndSavedAsDtoTest() {
        when(wishlistRepository.add(any())).thenReturn(WishlistItem.fromManual(1L, "연필", 500));

        CreateWishlistItemRequest requestDto = new CreateWishlistItemRequest("  연필  ", 500);
        WishlistItemResponse responseDto = wishlistService.add(requestDto);
        assertEquals(createManualResponse(1L, "연필", 500), responseDto);

        verify(wishlistRepository).add(any());
    }

    @Test
    @DisplayName("비정상 입력(빈 이름, 음수 가격, null 가격)은 거부하고 Repository를 호출하지 않음")
    void invalidInputTest() {
        assertThrows(IllegalArgumentException.class,
                () -> wishlistService.add(new CreateWishlistItemRequest(" ", 3000)));
        assertThrows(IllegalArgumentException.class,
                () -> wishlistService.add(new CreateWishlistItemRequest("노트", -1)));
        assertThrows(IllegalArgumentException.class,
                () -> wishlistService.add(new CreateWishlistItemRequest("노트", null)));

        verifyNoInteractions(wishlistRepository);
    }

    @Test
    @DisplayName("전체 목록을 DTO 목록으로 변환해 반환함")
    void getAllReturnsDtosTest() {
        when(wishlistRepository.getAll()).thenReturn(List.of(
                WishlistItem.fromManual(1L, "연필", 500),
                WishlistItem.fromManual(2L, "노트", 1000),
                WishlistItem.fromManual(3L, "펜", 800)
        ));

        assertEquals(List.of(
                        createManualResponse(1L, "연필", 500),
                        createManualResponse(2L, "노트", 1000),
                        createManualResponse(3L, "펜", 800)
                ),
                wishlistService.getAll());
    }

    @Test
    @DisplayName("id로 조회한 아이템을 DTO로 반환함")
    void findByIdReturnsDtoTest() {
        Long id = 1L;
        when(wishlistRepository.findById(id)).thenReturn(WishlistItem.fromManual(id, "연필", 500));

        assertEquals(createManualResponse(id, "연필", 500), wishlistService.findById(id));
    }

    @Test
    @DisplayName("존재하지 않는 id로 조회하면 EntityNotFoundException 예외 전파")
    void findNonExistentIdTest() {
        when(wishlistRepository.findById(any())).thenThrow(WishlistItemNotFoundException.class);

        assertThrows(WishlistItemNotFoundException.class, () -> wishlistService.findById(99L));
    }

    @Test
    @DisplayName("음수 예산은 허용하지 않고 Repository를 호출하지 않음")
    void negativeBudgetTest() {
        assertThrows(IllegalArgumentException.class, () -> wishlistService.recommend(-5000));

        verifyNoInteractions(wishlistRepository);
    }

    @Test
    @DisplayName("추천 결과를 DTO로 반환함")
    void recommendTest() {
        when(wishlistRepository.recommend(1000))
                .thenReturn(List.of(WishlistItem.fromManual(1L, "연필", 500)));
        when(wishlistRepository.recommend(300))
                .thenReturn(List.of());

        assertEquals(List.of(createManualResponse(1L, "연필", 500)), wishlistService.recommend(1000));
        assertEquals(List.of(), wishlistService.recommend(300));
    }

    private WishlistItemResponse createManualResponse(Long id, String name, Integer price) {
        return new WishlistItemResponse(id, SourceType.MANUAL, null, name, price, null);
    }

    @Test
    @DisplayName("외부 상품을 위시리스트로 정상적으로 가져올 수 있어야 함")
    void importSuccessTest() {
        Long externalProductId = 1L;
        Long wishlistItemId = 10L;

        // 외부 상품
        ExternalProduct externalProduct = new ExternalProduct(externalProductId, "iPhone", new BigDecimal("9.99"), "smartphones", "http://img", 3);

        when(wishlistRepository.existsByExternalId(any())).thenReturn(false);
        when(externalProductProvider.findById(any())).thenReturn(externalProduct);

        WishlistItem wishlistItem = WishlistItem.fromExternal(externalProduct);
        wishlistItem.setId(wishlistItemId);
        when(wishlistRepository.add(any())).thenReturn(wishlistItem);

        // 외부 상품을 가져오면 외부 id와 로컬 id를 구분해 저장하고 가격을 환산함
        WishlistItemResponse wishlistItemResponse = wishlistService.importExternal(wishlistItemId);
        assertEquals(wishlistItemId, wishlistItemResponse.id());
        assertEquals(externalProductId, wishlistItemResponse.externalId());
        assertEquals(SourceType.EXTERNAL, wishlistItemResponse.sourceType());
        assertEquals(9990, wishlistItemResponse.price());
        assertEquals(3, wishlistItemResponse.stock());
    }

    @Test
    @DisplayName("이미 가져온 외부 상품은 위시리스트에 중복 등록하지 않음")
    void importDuplicateTest(){
        // 외부 상품
        ExternalProduct externalProduct = new ExternalProduct(1L, "iPhone", new BigDecimal("9.99"), "smartphones", "http://img", 3);

        when(wishlistRepository.existsByExternalId(1L)).thenReturn(true);

        // 중복 id 등록 요청 시 예외를 반환
        // 외부 상품 정보를 조회하지 않아야 하고 위시리스트에 저장하지 않아야 함
        assertThrows(DuplicateWishlistItemException.class, () -> wishlistService.importExternal(1L));
        verifyNoInteractions(externalProductProvider);
        verify(wishlistRepository, never()).add(any());
    }

    @Test
    @DisplayName("외부 상품 조회가 실패하면 위시리스트에 저장하지 않음")
    void importFailsWhenProviderFailsTest() {
        when(wishlistRepository.existsByExternalId(any())).thenReturn(false);
        when(externalProductProvider.findById(any())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        assertThrows(ResponseStatusException.class, () -> wishlistService.importExternal(1L));
        verify(wishlistRepository, never()).add(any());
    }
}
