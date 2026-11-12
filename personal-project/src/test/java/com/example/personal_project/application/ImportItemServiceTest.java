package com.example.personal_project.application;

import com.example.personal_project.application.port.ProductClient;
import com.example.personal_project.domain.item.model.*;
import com.example.personal_project.domain.product.model.PriceConverter;
import com.example.personal_project.global.exception.*;
import com.example.personal_project.infrastructure.repository.ListItemRepository;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ImportItemServiceTest {
    @Test void externalFailuresNeverModifySavedItems() {
        var client=mock(ProductClient.class);
        var repository=new ListItemRepository();
        repository.add(new CollectionItem(null,"기존 상품",1000L,SourceType.MANUAL,null,null));
        var before=repository.findAll();
        var service=new ImportItemService(client,repository,new PriceConverter());
        when(client.findById(1L)).thenThrow(new ExternalProductNotFoundException(1L));
        when(client.findById(2L)).thenThrow(new ExternalApiException("timeout"));
        assertThrows(ExternalProductNotFoundException.class,()->service.importProduct(1L));
        assertEquals(before,repository.findAll());
        assertThrows(ExternalApiException.class,()->service.importProduct(2L));
        assertEquals(before,repository.findAll());
        assertThrows(IllegalArgumentException.class,()->service.importProduct(0L));
        verify(client,never()).findById(0L);
    }
}
