package com.example.personal_project.presentation.controller;

import com.example.personal_project.application.CartService;
import com.example.personal_project.domain.item.model.CollectionItem;
import com.example.personal_project.domain.item.model.SourceType;
import com.example.personal_project.global.exception.handler.GlobalExceptionHandler;
import com.example.personal_project.infrastructure.repository.ListItemRepository;
import com.example.personal_project.infrastructure.repository.MemoryCartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

class CartControllerTest {
    private MockMvc mvc;
    private CartService service;
    private ListItemRepository items;

    @BeforeEach
    void setUp() {
        items = new ListItemRepository();
        service = new CartService(items, new MemoryCartRepository(), new com.example.personal_project.domain.cart.model.CartAmountCalculator());
        mvc = MockMvcBuilders.standaloneSetup(new CartController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    private Long seed(SourceType source, Integer stock) {
        return items.add(new CollectionItem(null, "상품", 10000L, source,
                source == SourceType.MANUAL ? null : 10L, stock)).id();
    }

    private void addItem(Long id, int quantity, int status) throws Exception {
        mvc.perform(post("/cart/items").contentType(MediaType.APPLICATION_JSON)
                .content("{\"itemId\":" + id + ",\"quantity\":" + quantity + "}"))
                .andExpect(status().is(status));
    }

    @Test
    void addMergeChangeAndDeleteByCartRowId() throws Exception {
        seed(SourceType.MANUAL, null);
        Long itemId = seed(SourceType.MANUAL, null);
        addItem(itemId, 2, 200);
        addItem(itemId, 1, 200);
        mvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].cartItemId").value(1))
                .andExpect(jsonPath("$[0].itemId").value(itemId.intValue()))
                .andExpect(jsonPath("$[0].quantity").value(3));
        mvc.perform(patch("/cart/items/1").contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":5}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].quantity").value(5));
        mvc.perform(delete("/cart/items/1")).andExpect(status().isNoContent());
        assertTrue(service.findAll().isEmpty());
    }

    @Test
    void invalidInputAndMissingResources() throws Exception {
        for (String body : new String[]{"{}", "{\"itemId\":1}",
                "{\"itemId\":0,\"quantity\":1}", "{\"itemId\":1,\"quantity\":6}"}) {
            mvc.perform(post("/cart/items").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(patch("/cart/items/1").contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":\"abc\"}" )).andExpect(status().isBadRequest());
        addItem(999L, 1, 404);
        mvc.perform(patch("/cart/items/999").contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":1}" )).andExpect(status().isNotFound());
        mvc.perform(delete("/cart/items/999")).andExpect(status().isNotFound());
        assertTrue(service.findAll().isEmpty());
    }

    @Test
    void stockAndQuantityConflictsPreserveState() throws Exception {
        Long id = seed(SourceType.DUMMY_JSON, 3);
        addItem(id, 3, 200);
        var before = service.findAll();
        addItem(id, 1, 409);
        mvc.perform(patch("/cart/items/1").contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":4}" )).andExpect(status().isConflict());
        assertEquals(before, service.findAll());
        addItem(seed(SourceType.DUMMY_JSON, 0), 1, 409);
        assertEquals(before, service.findAll());
        Long manual = seed(SourceType.MANUAL, null);
        addItem(manual, 5, 200);
        before = service.findAll();
        addItem(manual, 1, 409);
        assertEquals(before, service.findAll());
    }

    @Test
    void twentyProductLimitAndRepositoryCopyIsolation() throws Exception {
        for (int i = 0; i < 20; i++) addItem(seed(SourceType.MANUAL, null), 1, 200);
        var before = service.findAll();
        addItem(seed(SourceType.MANUAL, null), 1, 409);
        assertEquals(before, service.findAll());
        assertThrows(UnsupportedOperationException.class, () -> before.clear());
        MemoryCartRepository repository = new MemoryCartRepository();
        var cart = repository.find();
        cart.add(items.findById(1L), 1);
        assertTrue(repository.find().findAll().isEmpty());
        repository.save(cart);
        cart.remove(1L);
        assertEquals(1, repository.find().findAll().size());
    }
}
