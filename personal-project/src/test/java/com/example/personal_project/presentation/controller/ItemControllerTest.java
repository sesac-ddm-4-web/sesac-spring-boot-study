package com.example.personal_project.presentation.controller;

import com.example.personal_project.application.ImportItemService;
import com.example.personal_project.application.ItemService;
import com.example.personal_project.global.exception.handler.GlobalExceptionHandler;
import com.example.personal_project.infrastructure.repository.ListItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ItemControllerTest {
    @Test
    void registrationReturnsSavedItemAndGetRequestsNeedNoBody() throws Exception {
        var repository = new ListItemRepository();
        var mvc = MockMvcBuilders.standaloneSetup(new ItemController(
                new ItemService(repository), mock(ImportItemService.class)))
                .setControllerAdvice(new GlobalExceptionHandler()).build();

        mvc.perform(post("/items").contentType("application/json")
                        .content("""
                                {"name":"필통","price":2000,"stock":10}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.stock").value(10));

        mvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("필통"))
                .andExpect(jsonPath("$[0].stock").value(10));
        mvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(10));
        mvc.perform(get("/items/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ITEM_NOT_FOUND"));
    }
}
