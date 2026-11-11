package com.example.demo.item.service;

import com.example.demo.global.exception.EntityNotFoundException;
import com.example.demo.item.dto.req.CollectionItemReq;
import com.example.demo.item.dto.res.CollectionItemRes;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD) // 테스트마다 빈 저장소로 시작
class SimpleCollectionItemServiceTest {

    @Autowired
    SimpleCollectionItemService simpleCollectionItemService;

    private void addSampleItems() {
        simpleCollectionItemService.add(new CollectionItemReq("코트", 120000));
        simpleCollectionItemService.add(new CollectionItemReq("증정 양말", 0));
        simpleCollectionItemService.add(new CollectionItemReq("흰 티셔츠", 15000));
        simpleCollectionItemService.add(new CollectionItemReq("줄무늬 티셔츠", 15000));
        simpleCollectionItemService.add(new CollectionItemReq("청바지", 50000));
    }

    @Test
    @DisplayName("아이템을 추가한 후 id로 조회하면 해당 아이템이 조회되어야 한다.")
    void collectionItemAddAndFindByIdTest() {
        CollectionItemReq collectionItemDto = new CollectionItemReq("청바지", 50000);

        CollectionItemRes savedCollectionItemDto = simpleCollectionItemService.add(collectionItemDto);
        Long savedCollectionItemId = savedCollectionItemDto.id();

        CollectionItemRes foundCollectionItemDto = simpleCollectionItemService.findById(savedCollectionItemId);

        assertNotNull(savedCollectionItemId);
        assertEquals(savedCollectionItemDto.id(), foundCollectionItemDto.id());
        assertEquals(savedCollectionItemDto.name(), foundCollectionItemDto.name());
        assertEquals(savedCollectionItemDto.price(), foundCollectionItemDto.price());
    }

    @Test
    @DisplayName("전체 목록을 조회하면 등록한 아이템이 등록 순서대로 조회되어야 한다.")
    void findAllTest() {
        addSampleItems();

        List<CollectionItemRes> collectionItemDtos = simpleCollectionItemService.findAll();

        assertEquals(
                List.of("코트", "증정 양말", "흰 티셔츠", "줄무늬 티셔츠", "청바지"),
                collectionItemDtos.stream().map(CollectionItemRes::name).toList()
        );
    }

    @Test
    @DisplayName("존재하지 않는 아이템 id로 조회하면 EntityNotFoundException이 발생해야 한다.")
    void findCollectionItemNotExistIdTest() {
        Long notExistId = -1L;

        assertThrows(EntityNotFoundException.class, () -> {
            simpleCollectionItemService.findById(notExistId);
        });
    }

    @Test
    @DisplayName("이름의 앞뒤 공백은 제거되어 저장되어야 한다.")
    void nameStripTest() {
        CollectionItemRes savedCollectionItemDto = simpleCollectionItemService.add(new CollectionItemReq("  흰 티셔츠  ", 15000));

        assertEquals("흰 티셔츠", savedCollectionItemDto.name());
    }

    @Test
    @DisplayName("빈 이름, 공백만 있는 이름, null 이름은 등록할 수 없고 기존 목록은 바뀌지 않아야 한다.")
    void blankNameTest() {
        simpleCollectionItemService.add(new CollectionItemReq("코트", 120000));

        assertThrows(ConstraintViolationException.class, () -> {
            simpleCollectionItemService.add(new CollectionItemReq("", 1000));
        });
        assertThrows(ConstraintViolationException.class, () -> {
            simpleCollectionItemService.add(new CollectionItemReq("   ", 1000));
        });
        assertThrows(ConstraintViolationException.class, () -> {
            simpleCollectionItemService.add(new CollectionItemReq(null, 1000));
        });

        assertEquals(1, simpleCollectionItemService.findAll().size());
    }

    @Test
    @DisplayName("음수 가격은 등록할 수 없고 기존 목록은 바뀌지 않아야 한다.")
    void negativePriceTest() {
        simpleCollectionItemService.add(new CollectionItemReq("코트", 120000));

        assertThrows(ConstraintViolationException.class, () -> {
            simpleCollectionItemService.add(new CollectionItemReq("청바지", -1));
        });

        assertEquals(1, simpleCollectionItemService.findAll().size());
    }

    @Test
    @DisplayName("가격이 0원인 무료 아이템은 등록할 수 있어야 한다.")
    void zeroPriceTest() {
        CollectionItemRes savedCollectionItemDto = simpleCollectionItemService.add(new CollectionItemReq("증정 양말", 0));

        assertEquals(0, savedCollectionItemDto.price());
    }

    @Test
    @DisplayName("음수 예산으로 추천하면 ConstraintViolationException이 발생해야 한다.")
    void negativeBudgetTest() {
        assertThrows(ConstraintViolationException.class, () -> {
            simpleCollectionItemService.recommend(-1);
        });
    }

    @Test
    @DisplayName("예산 15,000원이면 증정 양말, 흰 티셔츠, 줄무늬 티셔츠가 추천되어야 한다.")
    void recommendTest() {
        addSampleItems();

        List<CollectionItemRes> recommended = simpleCollectionItemService.recommend(15000);

        assertEquals(
                List.of("증정 양말", "흰 티셔츠", "줄무늬 티셔츠"),
                recommended.stream().map(CollectionItemRes::name).toList()
        );
    }

    @Test
    @DisplayName("예산 0원이면 무료 아이템만 추천되어야 한다.")
    void zeroBudgetTest() {
        addSampleItems();

        List<CollectionItemRes> recommended = simpleCollectionItemService.recommend(0);

        assertEquals(List.of("증정 양말"), recommended.stream().map(CollectionItemRes::name).toList());
    }

    @Test
    @DisplayName("추천을 조회해도 원본 목록의 순서와 내용은 바뀌지 않아야 한다.")
    void recommendKeepsOriginalListTest() {
        addSampleItems();
        List<CollectionItemRes> before = simpleCollectionItemService.findAll();

        simpleCollectionItemService.recommend(15000);

        assertEquals(before, simpleCollectionItemService.findAll());
    }

    @Test
    @DisplayName("아이템을 수정하면 같은 id로 수정된 값이 조회되어야 한다.")
    void updateTest() {
        CollectionItemRes saved = simpleCollectionItemService.add(new CollectionItemReq("코트", 120000));

        simpleCollectionItemService.update(saved.id(), new CollectionItemReq("롱코트", 150000));

        CollectionItemRes found = simpleCollectionItemService.findById(saved.id());
        assertEquals("롱코트", found.name());
        assertEquals(150000, found.price());
    }

    @Test
    @DisplayName("잘못된 값으로 수정하면 거부되고 기존 값은 유지되어야 한다.")
    void updateInvalidTest() {
        CollectionItemRes saved = simpleCollectionItemService.add(new CollectionItemReq("코트", 120000));

        assertThrows(ConstraintViolationException.class, () -> {
            simpleCollectionItemService.update(saved.id(), new CollectionItemReq(" ", -1));
        });

        CollectionItemRes found = simpleCollectionItemService.findById(saved.id());
        assertEquals("코트", found.name());
        assertEquals(120000, found.price());
    }

    @Test
    @DisplayName("존재하지 않는 아이템 id를 수정하면 EntityNotFoundException이 발생해야 한다.")
    void updateNotExistIdTest() {
        assertThrows(EntityNotFoundException.class, () -> {
            simpleCollectionItemService.update(-1L, new CollectionItemReq("코트", 120000));
        });
    }

    @Test
    @DisplayName("아이템을 삭제하면 해당 id로 조회할 수 없어야 한다.")
    void deleteTest() {
        CollectionItemRes saved = simpleCollectionItemService.add(new CollectionItemReq("코트", 120000));

        simpleCollectionItemService.delete(saved.id());

        assertThrows(EntityNotFoundException.class, () -> {
            simpleCollectionItemService.findById(saved.id());
        });
    }
}
