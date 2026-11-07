package com.example.demo.application;

import com.example.demo.domain.CollectionItem;
import com.example.demo.domain.CollectionItemRepository;
import com.example.demo.presentation.CollectionItemDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SimpleCollectionItemServiceUnitTest {

    @Mock
    private CollectionItemRepository collectionItemRepository;

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private SimpleCollectionItemService simpleCollectionItemService;

    private List<CollectionItem> sampleItems() {
        return new ArrayList<>(List.of(
                new CollectionItem(1L, "코트", 120000),
                new CollectionItem(2L, "증정 양말", 0),
                new CollectionItem(3L, "흰 티셔츠", 15000),
                new CollectionItem(4L, "줄무늬 티셔츠", 15000),
                new CollectionItem(5L, "청바지", 50000)
        ));
    }

    private List<String> names(List<CollectionItemDto> collectionItemDtos) {
        return collectionItemDtos.stream().map(CollectionItemDto::name).toList();
    }

    @Test
    @DisplayName("아이템 추가 후에는 추가된 아이템이 반환되어야 한다.")
    void collectionItemAddTest() {
        CollectionItemDto collectionItemDto = new CollectionItemDto(null, "청바지", 50000);
        Long COLLECTION_ITEM_ID = 1L;

        CollectionItem collectionItem = CollectionItemDto.toEntity(collectionItemDto);
        collectionItem.setId(COLLECTION_ITEM_ID);
        when(collectionItemRepository.add(any())).thenReturn(collectionItem);

        CollectionItemDto savedCollectionItemDto = simpleCollectionItemService.add(collectionItemDto);

        assertTrue(savedCollectionItemDto.id().equals(COLLECTION_ITEM_ID));
        assertTrue(savedCollectionItemDto.name().equals(collectionItemDto.name()));
        assertTrue(savedCollectionItemDto.price().equals(collectionItemDto.price()));
    }

    @Test
    @DisplayName("가격이 예산과 같은 아이템은 추천에 포함되고, 예산보다 1원 비싸면 제외되어야 한다.")
    void recommendBudgetBoundaryTest() {
        when(collectionItemRepository.findAll()).thenReturn(sampleItems());

        assertTrue(names(simpleCollectionItemService.recommend(15000)).contains("흰 티셔츠"));
        assertEquals(List.of("증정 양말"), names(simpleCollectionItemService.recommend(14999)));
    }

    @Test
    @DisplayName("추천 결과는 가격 오름차순이고, 같은 가격이면 등록 순서를 유지해야 한다.")
    void recommendSortTest() {
        when(collectionItemRepository.findAll()).thenReturn(List.of(
                new CollectionItem(1L, "청바지", 50000),
                new CollectionItem(2L, "흰 티셔츠", 15000),
                new CollectionItem(3L, "증정 양말", 0),
                new CollectionItem(4L, "줄무늬 티셔츠", 15000)
        ));

        List<CollectionItemDto> recommended = simpleCollectionItemService.recommend(15000);

        assertEquals(List.of("증정 양말", "흰 티셔츠", "줄무늬 티셔츠"), names(recommended));
    }

    @Test
    @DisplayName("예산 안의 아이템이 3개보다 많아도 최대 3개만 추천되어야 한다.")
    void recommendMaxThreeTest() {
        when(collectionItemRepository.findAll()).thenReturn(sampleItems());

        List<CollectionItemDto> recommended = simpleCollectionItemService.recommend(1000000);

        assertEquals(3, recommended.size());
        assertEquals(List.of("증정 양말", "흰 티셔츠", "줄무늬 티셔츠"), names(recommended));
    }

    @Test
    @DisplayName("예산 안에 살 수 있는 아이템이 없으면 빈 목록이 반환되어야 한다.")
    void recommendEmptyTest() {
        when(collectionItemRepository.findAll()).thenReturn(List.of(
                new CollectionItem(1L, "코트", 120000),
                new CollectionItem(2L, "청바지", 50000)
        ));

        List<CollectionItemDto> recommended = simpleCollectionItemService.recommend(10000);

        assertTrue(recommended.isEmpty());
    }

    @Test
    @DisplayName("추천을 조회해도 저장소에서 받은 원본 목록은 정렬되거나 바뀌지 않아야 한다.")
    void recommendKeepsOriginalListTest() {
        List<CollectionItem> original = sampleItems();
        List<CollectionItem> before = List.copyOf(original);
        when(collectionItemRepository.findAll()).thenReturn(original);

        simpleCollectionItemService.recommend(15000);

        assertEquals(before, original);
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L), original.stream().map(CollectionItem::getId).toList());
    }
}
