package com.example.personal_project.application;

import com.example.personal_project.domain.item.model.CollectionItem;
import com.example.personal_project.domain.item.model.SourceType;
import com.example.personal_project.presentation.dto.item.ItemDto;
import jdk.jfr.Description;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import  static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Test
    @Description("Service에 등록한 상품 추천 기능")
    void serviceIntegrationTest() {
        itemService.add(ItemDto.from(new CollectionItem(null, "무료 샘플", 0L, SourceType.MANUAL, null, null)));

        CollectionItem savedItem = itemService.recommendItems(0).stream()
                .filter(collectionItem -> collectionItem.name().equals("무료 샘플"))
                .findFirst()
                .orElseThrow();

        assertNotNull(savedItem.id());
        assertEquals(savedItem, itemService.findById(savedItem.id()));
        assertFalse(itemService.findAll().isEmpty());
    }
}
