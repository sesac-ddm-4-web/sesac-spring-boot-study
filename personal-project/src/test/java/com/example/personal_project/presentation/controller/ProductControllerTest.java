package com.example.personal_project.presentation.controller;

import com.example.personal_project.application.ProductService;
import com.example.personal_project.application.port.ProductClient;
import com.example.personal_project.domain.product.model.*;
import com.example.personal_project.global.exception.*;
import com.example.personal_project.global.exception.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductControllerTest {
    private ProductClient client;
    private MockMvc mvc;
    @BeforeEach void setUp() {
        client=mock(ProductClient.class);
        mvc=MockMvcBuilders.standaloneSetup(new ProductController(new ProductService(client)))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }
    @Test void defaultsAndEmptyListAreExposed() throws Exception {
        when(client.findAll(10,0)).thenReturn(new ExternalProductPage(List.of(),0,0,10));
        mvc.perform(get("/products")).andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isEmpty()).andExpect(jsonPath("$.limit").value(10));
        verify(client).findAll(10,0);
    }
    @Test void trimsSearchAndPreservesLimitSkipOrder() throws Exception {
        when(client.search("phone",20,7)).thenReturn(new ExternalProductPage(List.of(),0,7,20));
        mvc.perform(get("/products/search").param("q","  phone  ").param("limit","20").param("skip","7"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.skip").value(7));
        verify(client).search("phone",20,7);
        when(client.findAll(1,0)).thenReturn(new ExternalProductPage(List.of(),0,0,1));
        mvc.perform(get("/products").param("limit","1")).andExpect(status().isOk());
    }
    @Test void badInputsNeverCallExternalClient() throws Exception {
        for(String limit:List.of("0","21","abc")) mvc.perform(get("/products").param("limit",limit))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        mvc.perform(get("/products").param("skip","-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/products/search")).andExpect(status().isBadRequest());
        mvc.perform(get("/products/search").param("q"," ")).andExpect(status().isBadRequest());
        mvc.perform(get("/products/0")).andExpect(status().isBadRequest());
        verifyNoInteractions(client);
    }
    @Test void detailUsesExternalIdContractAndMapsExternalErrors() throws Exception {
        when(client.findById(42L)).thenReturn(new ExternalProduct(42L,"상품",new BigDecimal("9.99"),"beauty","image",0));
        mvc.perform(get("/products/42")).andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value(42)).andExpect(jsonPath("$.price").value(9.99))
                .andExpect(jsonPath("$.stock").value(0));
        when(client.findById(43L)).thenThrow(new ExternalProductNotFoundException(43L));
        mvc.perform(get("/products/43")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EXTERNAL_PRODUCT_NOT_FOUND"));
        when(client.findById(44L)).thenThrow(new ExternalApiException("internal"));
        mvc.perform(get("/products/44")).andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("EXTERNAL_API_FAILURE"));
    }
    @Test void pageDefensivelyCopiesInputList() {
        var input=new java.util.ArrayList<ExternalProduct>();
        input.add(new ExternalProduct(1L,"상품",BigDecimal.ONE,"category","image",1));
        var page=new ExternalProductPage(input,1,0,10);
        input.clear();
        assertEquals(1,page.products().size());
        assertThrows(UnsupportedOperationException.class,()->page.products().clear());
    }
}
