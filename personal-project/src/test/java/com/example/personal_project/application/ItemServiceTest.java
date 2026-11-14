package com.example.personal_project.application;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.example.personal_project.domain.SourceType;
import com.example.personal_project.domain.Item;
import com.example.personal_project.domain.EntityNotFoundException;
import com.example.personal_project.domain.ItemRepository;
import com.example.personal_project.infrastructure.MemoryItemRepository;
import com.example.personal_project.presentation.ItemDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest // 테스트를 실행할 때 Spring 애플리케이션의 설정을 읽고, 필요한 빈들을 생성하고 연결
public class ItemServiceTest {
    @Autowired
    ItemService itemService;

    @Autowired
    ItemRepository itemRepository;

    @Test
    @DisplayName("아이템 등록 시 이름 공백을 제거하고 실제 id로 조회한다")
    void 정상_등록_id_조회() {
        // 준비 — 등록할 데이터를 만든다
        ItemDto itemDto = new ItemDto(null, "    노트  ", 12000);
        // 실행 — 등록하고, 받은 id로 조회한다
        ItemDto savedItemDto = itemService.add(itemDto);
        Long savedItemId = savedItemDto.id();

        ItemDto foundItemDto = itemService.findById(savedItemId);
        // 검증 — 기대한 결과인지 확인한다
        assertNotNull(savedItemId);
        assertEquals("노트", savedItemDto.name());
        assertEquals(savedItemDto, foundItemDto);
    }

    @Test
    @DisplayName("없는 아이템 id로 조회하면 예외가 발생한다")
    void 없는_id_조회시_예외_발생() {
        Long notExistId = -1L;

        assertThrows(EntityNotFoundException.class, () -> {
            itemService.findById(notExistId);
        });
    }

    @Test
    @DisplayName("이름이 null이면 등록을 거부한다")
    void null_이름_등록시_예외_발생() {
        ItemDto itemDto = new ItemDto(null, null, 1000);
        // assertThrows(예상하는_예외, 실행할_코드);
        // () -> {}
        assertThrows(IllegalArgumentException.class, () -> {
            itemService.add(itemDto);
        });
    }

    @Test
    @DisplayName("이름이 빈 문자열이면 등록을 거부한다")
    void 빈_이름_등록시_예외_발생() {
        ItemDto itemDto = new ItemDto(null, "", 1000);

        assertThrows(IllegalArgumentException.class, () -> {
            itemService.add(itemDto);
        });
    }

    @Test
    @DisplayName("공백만 있는 이름으로 등록하면 예외가 발생한다")
    void 공백_이름_등록시_예외_발생() {
        ItemDto itemDto = new ItemDto(null, "   ", 1000);

        assertThrows(IllegalArgumentException.class, () -> {
            itemService.add(itemDto);
        });
    }

    @Test
    @DisplayName("음수 가격으로 등록하면 예외가 발생한다")
    void 음수_가격_등록시_예외_발생() {
        ItemDto itemDto = new ItemDto(null, "노트", -1);

        assertThrows(IllegalArgumentException.class, () -> {
            itemService.add(itemDto);
        });
    }

    @Test
    @DisplayName("등록에 실패하면 기존 목록이 유지된다")
    void 등록_실패시_기존_목록_유지() {
        itemService.add(new ItemDto(null, "노트", 12000));
        List<ItemDto> before = itemService.findAll();

        assertThrows(IllegalArgumentException.class, () -> {
            itemService.add(new ItemDto(null, "   ", 1000));
        });

        List<ItemDto> after = itemService.findAll();

        assertEquals(before, after);
    }
    @Test
    @DisplayName("음수 예산으로 추천하면 예외가 발생한다")
    void 음수_예산_추천시_예외_발생() {
        int budget = -1;

        assertThrows(IllegalArgumentException.class, () -> {
            itemService.recommend(budget);
        });
    }

    @Test
    @DisplayName("추천을 조회해도 원본 목록은 바뀌지 않는다")
    void 추천_조회_전후_원본_목록_유지() {
        itemService.add(new ItemDto(null, "노트", 12000));
        itemService.add(new ItemDto(null, "무료 샘플", 0));
        itemService.add(new ItemDto(null, "펜", 5000));

        List<ItemDto> before = itemService.findAll();

        itemService.recommend(5000);

        List<ItemDto> after = itemService.findAll();

        assertEquals(before, after);
    }

    @Test
    @DisplayName("검색어의 앞뒤 공백을 제거하고 이름 부분 검색을 한다")
    void 이름_부분_검색() {
        ItemDto savedItem = itemService.add(
                new ItemDto(null, "캠핑 랜턴", 20000)
        );

        List<ItemDto> result = itemService.findByNameContaining("  랜턴  ");

        assertTrue(result.contains(savedItem));
    }

    @Test
    @DisplayName("빈 검색어로 검색하면 예외가 발생한다")
    void 빈_검색어_검색시_예외_발생() {
        assertThrows(IllegalArgumentException.class, () -> {
            itemService.findByNameContaining("");
        });
    }

    @Test
    @DisplayName("공백만 있는 검색어로 검색하면 예외가 발생한다")
    void 공백_검색어_검색시_예외_발생() {
        assertThrows(IllegalArgumentException.class, () -> {
            itemService.findByNameContaining("   ");
        });
    }

    @Test
    @DisplayName("이름이 일치하는 아이템이 없으면 빈 목록을 반환한다")
    void 검색_결과가_없으면_빈_목록_반환() {
        List<ItemDto> result =
                itemService.findByNameContaining("존재하지않는검색어");

        assertEquals(List.of(), result);
    }

    @Test
    @DisplayName("수동 상품의 이름과 가격을 수정한다")
    void 수동_상품의_이름과_가격_수정() {
        ItemDto createDto = new ItemDto(null, "연필", 1000);
        ItemDto savedDto = itemService.add(createDto);

        ItemDto updateDto = new ItemDto(null, " 볼펜 ", 2000);
        ItemDto updatedDto = itemService.update(savedDto.id(), updateDto);

        ItemDto foundDto = itemService.findById(savedDto.id());

        assertEquals(savedDto.id(), foundDto.id()); // 수정해도 원래 상품의 id가 유지됐는지 확인
        assertEquals("볼펜", foundDto.name());
        assertEquals(Integer.valueOf(2000), foundDto.price());
        assertEquals(updatedDto, foundDto); // 수정했다고 반환한 내용이 실제 저장된 내용과 같은지 확인
    }

    @Test
    @DisplayName("외부 상품 수정은 409로 거부하고 저장된 정보를 유지한다")
    void 외부_상품_수정_거부시_원본_유지() {
        // 외부 서버를 호출하지 않고 실제 메모리 저장소에 외부 상품을 준비
        Item savedItem = itemRepository.add(new Item(
                null, "마스카라", 9990, SourceType.DUMMY_JSON, 1L, 99));

        // 수정 시도 전 목록
        List<ItemDto> before = itemService.findAll();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            itemService.update(savedItem.getId(), new ItemDto(null, "수정 시도", 100));
        });

        // 예외 상태가 409인지 확인
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        // 실패 후 저장 내용이 그대로인지
        assertEquals(before, itemService.findAll());

        // DTO에 없는 출처·외부 id·재고도 실제 저장소에서 확인
        Item foundItem = itemRepository.findById(savedItem.getId());
        assertEquals("마스카라", foundItem.getName());
        assertEquals(9990, foundItem.getPrice());
        assertEquals(SourceType.DUMMY_JSON, foundItem.getSourceType());
        assertEquals(Long.valueOf(1L), foundItem.getExternalId());
        assertEquals(Integer.valueOf(99), foundItem.getStock());
    }

    @Test
    @DisplayName("null 이름으로 수정하면 거부하고 기존 목록을 유지한다")
    void null_이름_수정시_기존_목록_유지() {
        ItemDto savedDto = itemService.add(new ItemDto(null, "연필", 1000));

        List<ItemDto> before = itemService.findAll();

        // IllegalArgumentException이 발생하는지 확인
        assertThrows(IllegalArgumentException.class, () -> {
            itemService.update(savedDto.id(), new ItemDto(null, null, 2000));
        });

        assertEquals(before, itemService.findAll());
    }

    @Test
    @DisplayName("빈 이름으로 수정하면 거부하고 기존 목록을 유지한다")
    void 빈_이름_수정시_기존_목록_유지() {
        ItemDto savedDto = itemService.add(new ItemDto(null, "연필", 1000));

        List<ItemDto> before = itemService.findAll();

        assertThrows(IllegalArgumentException.class, () -> {
            itemService.update(savedDto.id(), new ItemDto(null, "", 2000));
        });

        assertEquals(before, itemService.findAll());
    }

    @Test
    @DisplayName("공백 이름으로 수정하면 거부하고 기존 목록을 유지한다")
    void 공백_이름_수정시_기존_목록_유지() {
        ItemDto savedDto = itemService.add(new ItemDto(null, "연필", 1000));
        List<ItemDto> before = itemService.findAll();

        assertThrows(IllegalArgumentException.class, () -> {
            itemService.update(savedDto.id(), new ItemDto(null, "   ", 2000));
        });

        assertEquals(before, itemService.findAll());
    }

    @Test
    @DisplayName("음수 가격으로 수정하면 이름도 바뀌지 않고 기존 목록을 유지한다")
    void 음수_가격_수정시_기존_목록_유지() {
        ItemDto savedDto = itemService.add(new ItemDto(null, "연필", 1000));
        List<ItemDto> before = itemService.findAll();

        // 이름도 바뀌면 안 된다
        assertThrows(IllegalArgumentException.class, () -> {
            itemService.update(savedDto.id(), new ItemDto(null, "바뀌면 안 되는 이름", -1));
        });

        assertEquals(before, itemService.findAll());
    }

    @Test
    @DisplayName("가격이 누락된 수정은 거부하고 기존 목록을 유지한다")
    void 가격_누락_수정시_기존_목록_유지() {
        ItemDto savedDto = itemService.add(new ItemDto(null, "연필", 1000));
        List<ItemDto> before = itemService.findAll();

        assertThrows(IllegalArgumentException.class, () -> {
            itemService.update(savedDto.id(), new ItemDto(null, "볼펜", null));
        });

        assertEquals(before, itemService.findAll());
    }

    @Test
    @DisplayName("없는 id로 수정하면 찾을 수 없음 예외가 발생하고 목록을 유지한다")
    void 없는_id_수정시_기존_목록_유지() {
        itemService.add(new ItemDto(null, "연필", 1000));
        List<ItemDto> before = itemService.findAll();

        assertThrows(EntityNotFoundException.class, () -> {
            itemService.update(-1L, new ItemDto(null, "볼펜", 2000));
        });

        assertEquals(before, itemService.findAll());
    }

    @Test
    @DisplayName("수동 상품을 0원으로 수정할 수 있고 수동 출처를 유지한다")
    void 수동_상품_0원_수정_허용() {
        ItemDto savedDto = itemService.add(new ItemDto(null, "연필", 1000));

        ItemDto updatedDto = itemService.update(
                savedDto.id(), new ItemDto(null, "무료 샘플", 0));

        ItemDto foundDto = itemService.findById(savedDto.id());

        assertEquals(savedDto.id(), foundDto.id());
        assertEquals("무료 샘플", foundDto.name());
        assertEquals(Integer.valueOf(0), foundDto.price());
        assertEquals(updatedDto, foundDto); // 실제로 저장됐는지 확인

        Item foundItem = itemRepository.findById(savedDto.id());

        assertEquals(SourceType.MANUAL, foundItem.getSourceType());
        assertNull(foundItem.getExternalId());
        assertNull(foundItem.getStock());
    }

    @Test
    @DisplayName("수정 요청 본문의 id 대신 전달받은 대상 id로 수정한다")
    void 수정_요청_본문의_id는_사용하지_않음() {
        ItemDto targetDto = itemService.add(new ItemDto(null, "연필", 1000));
        ItemDto otherDto = itemService.add(new ItemDto(null, "노트", 12000));

        List<ItemDto> before = itemService.findAll();

        ItemDto updatedDto = itemService.update(
                targetDto.id(), new ItemDto(otherDto.id(), "볼펜", 2000)); // target이 수정

        assertEquals(targetDto.id(), updatedDto.id());
        assertEquals(updatedDto, itemService.findById(targetDto.id()));
        assertEquals(otherDto, itemService.findById(otherDto.id()));

        // 기존 위치와 목록 개수도 그대로 유지
        List<ItemDto> after = itemService.findAll();
        assertEquals(before.size(), after.size());
        for (int i = 0; i < before.size(); i++) {
            assertEquals(before.get(i).id(), after.get(i).id());
        }
    }
}
