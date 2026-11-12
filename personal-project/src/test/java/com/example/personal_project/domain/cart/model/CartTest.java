package com.example.personal_project.domain.cart.model;

import com.example.personal_project.domain.item.model.*;
import com.example.personal_project.global.exception.*;
import com.example.personal_project.infrastructure.repository.MemoryCartRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CartTest {
    private CollectionItem item(long id,long price,Integer stock) {
        return new CollectionItem(id,"상품",price,stock==null?SourceType.MANUAL:SourceType.DUMMY_JSON,
                stock==null?null:id,stock);
    }
    @Test void fullCartStillAllowsExistingProductAndFreedSlotCanBeReused() {
        var cart=new Cart();
        for(long id=1;id<=20;id++) cart.add(item(id,100L,null),1);
        cart.add(item(1,100L,null),1);
        assertEquals(20,cart.findAll().size());
        assertEquals(2,cart.findAll().getFirst().quantity());
        var before=cart.findAll();
        assertThrows(CartConflictException.class,()->cart.add(item(21,100L,null),1));
        assertEquals(before,cart.findAll());
        cart.remove(20L);
        cart.add(item(21,100L,null),1);
        assertEquals(21L,cart.findAll().getLast().cartItemId());
    }
    @Test void snapshotSurvivesSourceReplacementAndReaddUsesCurrentValues() {
        var cart=new Cart();
        cart.add(item(1,100L,3),1);
        cart.add(item(1,200L,5),1);
        assertEquals(100L,cart.findAll().getFirst().unitPrice());
        assertEquals(3,cart.findAll().getFirst().stock());
        cart.remove(1L);
        cart.add(item(1,200L,5),1);
        assertEquals(200L,cart.findAll().getFirst().unitPrice());
        assertEquals(5,cart.findAll().getFirst().stock());
        assertEquals(2L,cart.findAll().getFirst().cartItemId());
    }
    @Test void failedMutationPreservesRowsAndNextId() {
        var cart=new Cart();
        assertThrows(CartConflictException.class,()->cart.add(item(1,100L,0),1));
        cart.add(item(2,100L,2),1);
        assertEquals(1L,cart.findAll().getFirst().cartItemId());
        var before=cart.findAll();
        assertThrows(CartConflictException.class,()->cart.changeQuantity(1L,3));
        assertThrows(CartItemNotFoundException.class,()->cart.remove(999L));
        assertEquals(before,cart.findAll());
    }
    @Test void repositoryCopiesRowsAndSequenceOnBothReadAndWrite() {
        var repository=new MemoryCartRepository();
        var cart=repository.find();
        cart.add(item(1,100L,null),1);
        repository.save(cart);
        var read=repository.find();
        read.changeQuantity(1L,5);
        assertEquals(1,repository.find().findAll().getFirst().quantity());
        cart.remove(1L);
        assertEquals(1,repository.find().findAll().size());
        read.add(item(2,200L,null),1);
        assertEquals(2L,read.findAll().getLast().cartItemId());
        repository.save(read);
        read.remove(2L);
        assertEquals(2,repository.find().findAll().size());
    }
}
