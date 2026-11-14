
package com.example.campkit.integration;

import com.example.campkit.domain.CollectionItem;
import com.example.campkit.service.CollectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CampkitApplicationTests {

    @Autowired
    private CollectionService service;

    @Test
    void 스프링_의존성_주입_확인() {
        CollectionItem item = service.register("캠핑 랜턴", 15000);

        assertNotNull(service);
        assertNotNull(item.getId());
        assertEquals("캠핑 랜턴", service.findById(item.getId()).getName());
    }
}
