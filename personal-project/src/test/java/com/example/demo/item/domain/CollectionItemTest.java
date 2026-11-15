package com.example.demo.item.domain;

import com.example.demo.global.exception.ConflictException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CollectionItemTest {

    @Test
    void 직접_등록한_상품은_수동_상품이고_외부_id와_재고가_없다() {
        CollectionItem item = new CollectionItem(1L, "코트", 120000);

        assertEquals(SourceType.MANUAL, item.getSourceType());
        assertNull(item.getExternalId());
        assertNull(item.getStock());
    }

    @Test
    void 외부_상품은_외부_id와_재고를_따로_가진다() {
        CollectionItem item = CollectionItem.ofExternal(7L, "Mascara", 9990, 99);

        assertEquals(SourceType.EXTERNAL, item.getSourceType());
        assertNull(item.getId());
        assertEquals(7L, item.getExternalId());
        assertEquals(99, item.getStock());
    }

    @Test
    void 외부_상품은_수정할_수_없다() {
        CollectionItem item = CollectionItem.ofExternal(7L, "Mascara", 9990, 99);

        assertThrows(ConflictException.class, item::checkEditable);
    }

    @Test
    void 수동_상품은_수정할_수_있다() {
        CollectionItem item = new CollectionItem(1L, "코트", 120000);

        assertDoesNotThrow(item::checkEditable);
    }
}
