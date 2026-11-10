package com.example.hobby_wishlist;

import com.example.hobby_wishlist.application.WishlistService;
import com.example.hobby_wishlist.domain.EntityNotFoundException;
import com.example.hobby_wishlist.domain.WishlistItem;
import com.example.hobby_wishlist.domain.WishlistRepository;
import com.example.hobby_wishlist.dto.CreateWishlistItemRequest;
import com.example.hobby_wishlist.dto.WishlistItemResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WishlistServiceUnitTests {

    @Mock
    private WishlistRepository wishlistRepository;

    @InjectMocks
    private WishlistService wishlistService;

    @Test
    @DisplayName("등록 시 이름의 앞뒤 공백은 제거하고 Repository가 저장한 결과를 DTO로 변환해 반환함")
    void stripNameAndSavedAsDtoTest() {
        when(wishlistRepository.add(any())).thenReturn(WishlistItem.fromManual(1L, "연필", 500));

        CreateWishlistItemRequest requestDto = new CreateWishlistItemRequest("  연필  ", 500);
        WishlistItemResponse responseDto = wishlistService.add(requestDto);
        assertEquals(new WishlistItemResponse(1L, "연필", 500), responseDto);

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
                        new WishlistItemResponse(1L, "연필", 500),
                        new WishlistItemResponse(2L, "노트", 1000),
                        new WishlistItemResponse(3L, "펜", 800)
                ),
                wishlistService.getAll());
    }

    @Test
    @DisplayName("id로 조회한 아이템을 DTO로 반환함")
    void findByIdReturnsDtoTest() {
        Long id = 1L;
        when(wishlistRepository.findById(id)).thenReturn(WishlistItem.fromManual(id, "연필", 500));

        assertEquals(new WishlistItemResponse(id, "연필", 500), wishlistService.findById(id));
    }

    @Test
    @DisplayName("존재하지 않는 id로 조회하면 EntityNotFoundException 예외 전파")
    void findNonExistentIdTest() {
        when(wishlistRepository.findById(any())).thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class, () -> wishlistService.findById(99L));
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

        assertEquals(List.of(new WishlistItemResponse(1L, "연필", 500)), wishlistService.recommend(1000));
        assertEquals(List.of(), wishlistService.recommend(300));
    }
}
