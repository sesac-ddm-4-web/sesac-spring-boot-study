package com.example.personal_project.application;

import com.example.personal_project.domain.Item;
import com.example.personal_project.domain.ItemRepository;
import com.example.personal_project.domain.SourceType;
import com.example.personal_project.infrastructure.DummyJsonClient;
import com.example.personal_project.infrastructure.DummyJsonProduct;
import com.example.personal_project.presentation.ItemDto;
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
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceUnitTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ValidationService validationService;
    @Mock
    private DummyJsonClient client;
    @InjectMocks
    private ItemService itemService;

    @Test
    @DisplayName("등록한 아이템을 반환한다")
    void 정상_아이템_등록() {
        when(validationService.validateName("노트")).thenReturn("노트");
        when(validationService.validatePrice(12000)).thenReturn(12000);

        Item savedItem = new Item(1L, "노트", 12000);
        // any(Item.class): 어떤 Item 객체가 인자로 들어와도
        // Repository의 add에 어떤 Item 객체가 들어오든, 준비해 둔 savedItem을 반환
        when(itemRepository.add(any(Item.class))).thenReturn(savedItem);

        ItemDto result = itemService.add(new ItemDto(null, "노트", 12000));
        assertEquals(ItemDto.from(savedItem), result);
    }

    @Test
    @DisplayName("이름 검증에 실패하면 저장하지 않는다")
    void 이름_검증_실패시_저장하지_않음() {
        when(validationService.validateName("   "))
                .thenThrow(new IllegalArgumentException("이름을 입력해주세요."));

        ItemDto itemDto = new ItemDto(null, "   ", 1000);

        assertThrows(IllegalArgumentException.class,
                () -> itemService.add(itemDto));

        verifyNoInteractions(itemRepository);// 저장소에 아무 요청도 하지 않았나?
    }

    @Test
    @DisplayName("예산이 0원이면 무료 아이템만 추천한다")
    void 예산_0원시_무료_아이템_추천() {
        // 가짜 검증 객체가 예산 0을 받으면, 검증을 통과한 값 0을 반환
        when(validationService.validateBudget(0)).thenReturn(0);

        List<Item> items = List.of(
                new Item(1L, "노트", 12000),
                new Item(2L, "무료 샘플", 0)
        );
        // Service가 가짜 Repository에 전체 목록을 요청하면, 두 아이템의 목록을 반환
        when(itemRepository.findAll()).thenReturn(items);

        List<ItemDto> result = itemService.recommend(0);

        List<ItemDto> expected = List.of(
                new ItemDto(2L, "무료 샘플", 0)
        );
        // 실제 추천 결과가 무료 샘플 하나뿐인 목록과 같은가?
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("예산 이하의 아이템을 가격순으로 최대 3개 추천한다")
    void 예산_이하_가격순_최대_3개_추천() {
        when(validationService.validateBudget(5000)).thenReturn(5000);

        List<Item> items = List.of(
                new Item(1L, "노트", 12000),
                new Item(2L, "무료 샘플", 0),
                new Item(3L, "펜", 5000),
                new Item(4L, "스티커", 5000),
                new Item(5L, "책갈피", 1000)
        );
        when(itemRepository.findAll()).thenReturn(items);

        List<ItemDto> result = itemService.recommend(5000);

        List<ItemDto> expected = List.of(
                new ItemDto(2L, "무료 샘플", 0),
                new ItemDto(5L, "책갈피", 1000),
                new ItemDto(3L, "펜", 5000)
        );
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("예산 이하의 아이템이 없으면 빈 목록을 반환한다")
    void 추천_후보가_없으면_빈_목록_반환() {
        when(validationService.validateBudget(1000)).thenReturn(1000);

        List<Item> items = List.of(
                new Item(1L, "노트", 12000),
                new Item(2L, "펜", 5000)
        );
        when(itemRepository.findAll()).thenReturn(items);

        List<ItemDto> result = itemService.recommend(1000);

        assertEquals(List.of(), result);
    }

    @Test
    @DisplayName("외부 상품을 원화로 환산하여 가져온다")
    void 외부_상품_가져오기_성공() {
        DummyJsonProduct product = new DummyJsonProduct(
                1L,
                "마스카라",
                new BigDecimal("9.99"),
                "beauty",
                "https://example.com/product.webp",
                99);

        when(client.findById(1L)).thenReturn(product);

        Item savedItem = new Item(10L,
                "마스카라",
                9990,
                SourceType.DUMMY_JSON,
                1L,
                99);

        when(itemRepository.add(any(Item.class))).thenReturn(savedItem);

        ItemDto result = itemService.importExternal(1L);

        // 저장 결과를 DTO로 반환했는지 확인
        assertEquals(ItemDto.from(savedItem), result);

        // Service가 저장 요청한 Item의 가격 확인
        verify(itemRepository).add(
                argThat(item -> item.getPrice() == 9990
                        && item.getSourceType() == SourceType.DUMMY_JSON
                        && Objects.equals(item.getExternalId(), 1L)
                        && Objects.equals(item.getStock(), 99)
                )
        );
    }

    @Test
    @DisplayName("이미 가져온 외부 상품은 중복 등록하지 않는다")
    void 이미_가져온_외부_상품은_등록_거부() {
        when(itemRepository.existsExternal(SourceType.DUMMY_JSON, 1L)).thenReturn(true);

        // 중복 상품 가져오기를 실행하면 ResponseStatusException이 발생해야 한다
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> itemService.importExternal(1L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        // 중복을 발견한 뒤 외부 상품 조회를 요청하지 않았는지 확인
        verifyNoInteractions(client);
    }

    @Test
    @DisplayName("품절 상품도 위시리스트에 가져올 수 있다")
    void 품절_외부_상품도_가져오기_허용() {
        DummyJsonProduct product = new DummyJsonProduct(
                1L,
                "마스카라",
                new BigDecimal("9.99"),
                "beauty",
                "https://example.com/product.webp",
                0);

        when(client.findById(1L)).thenReturn(product);

        Item savedItem = new Item(10L,
                "마스카라",
                9990,
                SourceType.DUMMY_JSON,
                1L,
                0);

        when(itemRepository.add(any(Item.class))).thenReturn(savedItem);

        ItemDto result = itemService.importExternal(1L);

        assertEquals(ItemDto.from(savedItem), result);

        verify(itemRepository).add(
                argThat(item -> item.getStock() == 0)
        );
    }

    @Test
    @DisplayName("외부 가격 환산 후 HALF_UP으로 반올림한다")
    void 외부_가격을_원_단위로_반올림() {
        DummyJsonProduct product = new DummyJsonProduct(
                1L,
                "마스카라",
                new BigDecimal("9.9905"),
                "beauty",
                "https://example.com/product.webp",
                99);

        when(client.findById(1L)).thenReturn(product);

        Item savedItem = new Item(10L,
                "마스카라",
                9991,
                SourceType.DUMMY_JSON,
                1L,
                99);

        when(itemRepository.add(any(Item.class))).thenReturn(savedItem);

        ItemDto result = itemService.importExternal(1L);

        // 저장 결과를 DTO로 반환했는지 확인
        assertEquals(ItemDto.from(savedItem), result);

        // Service가 저장 요청한 Item의 가격 확인
        verify(itemRepository).add(
                argThat(item -> item.getPrice() == 9991)
        );
    }

    @Test
    @DisplayName("외부 상품 조회에 실패하면 저장하지 않는다")
    void 외부_상품_조회_실패시_저장하지_않음() {
        // 외부 조회가 실패하는 상황을 준비
        when(client.findById(1L)).thenThrow(new ResponseStatusException (
                HttpStatus.BAD_GATEWAY,
                "외부 상품 정보를 가져오지 못했습니다."));

        // Service를 실행해서 예외가 전달되는지 확인
        assertThrows(ResponseStatusException.class, () -> {
            itemService.importExternal(1L);
        });

        // Repository를 사용했어도 괜찮지만, add()는 한 번도 호출하면 안 돼
        verify(itemRepository, never()).add(any(Item.class));
    }

    @Test
    @DisplayName("외부 상품 수정은 409로 거부하고 저장 요청을 하지 않는다")
    void 외부_상품_수정_거부() {
        Item existingItem = new Item(
                10L, "마스카라", 9990, SourceType.DUMMY_JSON, 1L, 99);

        when(itemRepository.findById(10L)).thenReturn(existingItem);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            itemService.update(10L, new ItemDto(null, "수정 시도", 100));
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        // Repository에 수정 저장을 요청하지 않았는지 확인
        verify(itemRepository, never()).update(any(Item.class));
        // 이름·가격 검증이나 외부 조회도 호출하지 않았는지 확인
        verifyNoInteractions(validationService, client);
    }
}
