package com.example.personal_project.application;

import com.example.personal_project.domain.item.model.CollectionItem;
import com.example.personal_project.domain.item.model.SourceType;
import com.example.personal_project.infrastructure.repository.ListItemRepository;
import com.example.personal_project.presentation.dto.item.ItemDto;
import jdk.jfr.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class ItemServiceTest {

    private ItemService itemService;
    private ListItemRepository listItemRepository;

    @BeforeEach
    void setUp() {
        listItemRepository = new ListItemRepository();
        itemService = new ItemService(listItemRepository);
    }

    private ItemDto request(String name, Integer price) {
        return ItemDto.from(new CollectionItem(null, name, price == null ? null : price.longValue(), SourceType.MANUAL, null, null));
    }

    private CollectionItem seed(String name, Integer price) {
        return listItemRepository.add(new CollectionItem(null, name, price == null ? null : price.longValue(), SourceType.MANUAL, null, null));
    }

    @Test
    @Description("등록 완료된 목록 조회")
    void findItemTest() {
        itemService.add(request("    필통    ", 300));

        assertEquals(1, itemService.findAll().size());

        CollectionItem savedItem = listItemRepository.findAll().get(0);
        assertNotNull(savedItem.id());
        assertEquals("필통", savedItem.name());
        assertEquals(300L, savedItem.price());
        assertEquals(savedItem, itemService.findById(savedItem.id()));
    }

    @Test
    @Description("등록 시 잘못된 입력 거부")
    void invalidRegisterInputTest() {
        seed("연필", 100);
        List<CollectionItem> before = List.copyOf(listItemRepository.findAll());

        assertThrows(IllegalArgumentException.class,
                () -> itemService.add(request(null, 100)));
        assertThrows(IllegalArgumentException.class,
                () -> itemService.add(request("", 100)));
        assertThrows(IllegalArgumentException.class,
                () -> itemService.add(request("    ", 100)));
        assertThrows(IllegalArgumentException.class,
                () -> itemService.add(request("필통", -1)));

        assertEquals(before, listItemRepository.findAll());
    }

    @Test
    @Description("없는 ID 및 음수 예산 거부")
    void invalidNumberInputTest() {
        assertThrows(NoSuchElementException.class,
                () -> itemService.findById(999L));
        assertThrows(IllegalArgumentException.class,
                () -> itemService.recommendItems(-1));
    }

    @Test
    @Description("가격순 정렬 및 등록순 최대 3개 추천 로직")
    void recommendTest() {
        seed("노트", 12000);
        seed("무료 샘플", 0);
        seed("펜", 5000);
        seed("스티커", 5000);
        seed("게임", 8000);

        List<CollectionItem> result = itemService.recommendItems(12000);
        assertEquals(List.of("무료 샘플", "펜", "스티커"), result.stream().map(CollectionItem::name).toList());
    }

    @Test
    @Description("예산과 금액이 같은 가격의 상품 추천 확인")
    void budgetEqualToPrice() {
        seed("예산 초과", 5001);
        CollectionItem boundaryItem = seed("펜", 5000);
        assertEquals(List.of(boundaryItem), itemService.recommendItems(5000));
    }

    @Test
    @Description("무료 아이템 등록 및 예산 0원일 때")
    void priceAmountZero() {
        itemService.add(request("무료 샘플", 0));
        seed("펜", 100);

        List<CollectionItem> result = itemService.recommendItems(0);

        assertEquals(1, result.size());
        assertEquals("무료 샘플", result.get(0).name());
        assertEquals(0L, result.get(0).price());
    }

    @Test
    @Description("추천할 아이템이 없는 경우")
    void recommendationNotMatched() {
        assertTrue(itemService.recommendItems(100).isEmpty());

        seed("게임", 8000);

        assertTrue(itemService.recommendItems(100).isEmpty());
    }

    @Test
    @Description("추천 조회 시 원본 보호")
    void notChangedAfterRecommend() {
        seed("노트", 12000);
        seed("펜", 5000);
        seed("무료 샘플", 0);

        List<CollectionItem> before = List.copyOf(listItemRepository.findAll());

        itemService.recommendItems(12000);

        assertEquals(before, listItemRepository.findAll());
    }
}