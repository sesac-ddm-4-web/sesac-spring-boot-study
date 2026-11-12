package com.example.personal_project.presentation.controller;

import com.example.personal_project.application.CartService;
import com.example.personal_project.domain.cart.model.CartAmountCalculator;
import com.example.personal_project.domain.item.model.CollectionItem;
import com.example.personal_project.domain.item.model.SourceType;
import com.example.personal_project.global.exception.handler.GlobalExceptionHandler;
import com.example.personal_project.infrastructure.repository.ListItemRepository;
import com.example.personal_project.infrastructure.repository.MemoryCartRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartPreviewControllerTest {
    @Test
    void previewReturnsAmountsAndNeverSavesOnSuccessOrFailure() throws Exception {
        var items = new ListItemRepository();
        var repository = spy(new MemoryCartRepository());
        var service = new CartService(items, repository, new CartAmountCalculator());
        Long id = items.add(new CollectionItem(null,"상품",110000L,SourceType.MANUAL,null,null)).id();
        service.add(id,1);
        var before = service.findAll();
        clearInvocations(repository);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new CartPreviewController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(get("/cart/preview")).andExpect(status().isOk())
                .andExpect(jsonPath("$.coupon").value("NONE"))
                .andExpect(jsonPath("$.finalAmount").value(110000));
        mvc.perform(get("/cart/preview").param("couponCode","PERCENT10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.subtotal").value(110000))
                .andExpect(jsonPath("$.discount").value(11000))
                .andExpect(jsonPath("$.discountedSubtotal").value(99000))
                .andExpect(jsonPath("$.shipping").value(5000))
                .andExpect(jsonPath("$.finalAmount").value(104000));
        mvc.perform(get("/cart/preview").param("couponCode","FIXED15000"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.finalAmount").value(100000));
        mvc.perform(get("/cart/preview").param("couponCode","UNKNOWN"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/cart/best-coupon")).andExpect(status().isOk())
                .andExpect(jsonPath("$.coupon").value("FIXED15000"))
                .andExpect(jsonPath("$.finalAmount").value(100000));
        assertEquals(before, service.findAll());
        verify(repository, never()).save(any());
    }

    @Test
    void emptyAndIneligibleCartReturn400WithoutChanges() throws Exception {
        var items = new ListItemRepository();
        var service = new CartService(items,new MemoryCartRepository(),new CartAmountCalculator());
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new CartPreviewController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(get("/cart/preview")).andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(0)).andExpect(jsonPath("$.shipping").value(0))
                .andExpect(jsonPath("$.finalAmount").value(0));
        for (String code : new String[]{"PERCENT10","FIXED15000",""}) {
            mvc.perform(get("/cart/preview").param("couponCode",code)).andExpect(status().isBadRequest());
        }
        Long id = items.add(new CollectionItem(null,"상품",99999L,SourceType.MANUAL,null,null)).id();
        service.add(id,1);
        var before=service.findAll();
        mvc.perform(get("/cart/preview").param("couponCode","FIXED15000"))
                .andExpect(status().isBadRequest());
        assertEquals(before,service.findAll());
    }
}
