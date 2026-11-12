package com.example.personal_project.presentation.controller;

import com.example.personal_project.application.*;
import com.example.personal_project.application.port.ProductClient;
import com.example.personal_project.domain.product.model.*;
import com.example.personal_project.domain.item.model.*;
import com.example.personal_project.global.exception.*;
import com.example.personal_project.global.exception.handler.GlobalExceptionHandler;
import com.example.personal_project.infrastructure.repository.ListItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ItemImportControllerTest {
    @Test void importsServerValuesAndRejectsDuplicateWithoutUpdatingSnapshot() throws Exception {
        var client=mock(ProductClient.class);
        var repository=new ListItemRepository();
        var service=new ImportItemService(client,repository,new PriceConverter());
        var mvc=MockMvcBuilders.standaloneSetup(new ItemController(new ItemService(repository),service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        when(client.findById(42L)).thenReturn(new ExternalProduct(42L,"서버 상품",new BigDecimal("9.99"),"beauty","image",0));
        mvc.perform(post("/items/import").contentType("application/json")
                .content("{\"externalId\":42,\"name\":\"위조 이름\",\"price\":1}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("서버 상품")).andExpect(jsonPath("$.price").value(9990));
        var before=repository.findAll();
        assertEquals(SourceType.DUMMY_JSON,before.getFirst().sourceType());
        assertEquals(42L,before.getFirst().externalId());
        assertEquals(0,before.getFirst().stock());
        mvc.perform(post("/items/import").contentType("application/json").content("{\"externalId\":42}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("DUPLICATE_EXTERNAL_PRODUCT"));
        verify(client,times(1)).findById(42L);
        assertEquals(before,repository.findAll());
    }
    @Test void importFailuresExposeRealHttpContractAndPreserveData() throws Exception {
        var client=mock(ProductClient.class);
        var repository=new ListItemRepository();
        repository.add(new CollectionItem(null,"기존",1L,SourceType.MANUAL,null,null));
        var before=repository.findAll();
        var service=new ImportItemService(client,repository,new PriceConverter());
        var mvc=MockMvcBuilders.standaloneSetup(new ItemController(new ItemService(repository),service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        for(String body:java.util.List.of("{}","{\"externalId\":0}","{\"externalId\":-1}","{\"externalId\":\"abc\"}"))
            mvc.perform(post("/items/import").contentType("application/json").content(body)).andExpect(status().isBadRequest());
        verifyNoInteractions(client);
        when(client.findById(1L)).thenThrow(new ExternalProductNotFoundException(1L));
        when(client.findById(2L)).thenThrow(new ExternalApiException("timeout"));
        mvc.perform(post("/items/import").contentType("application/json").content("{\"externalId\":1}"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/items/import").contentType("application/json").content("{\"externalId\":2}"))
                .andExpect(status().isBadGateway());
        assertEquals(before,repository.findAll());
    }
}
