package com.example.personal_project.infrastructure.repository;

import com.example.personal_project.domain.item.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ListItemRepositoryTest {
    @Test void localIdsAreIndependentAndDuplicateCheckUsesSourceAndExternalId() {
        var repository=new ListItemRepository();
        var saved=repository.add(new CollectionItem(null,"외부",9990L,SourceType.DUMMY_JSON,42L,0));
        assertEquals(1L,saved.id());
        assertEquals(42L,saved.externalId());
        assertEquals(0,saved.stock());
        assertTrue(repository.existsBySourceTypeAndExternalId(SourceType.DUMMY_JSON,42L));
        assertFalse(repository.existsBySourceTypeAndExternalId(SourceType.DUMMY_JSON,43L));
        assertFalse(repository.existsBySourceTypeAndExternalId(SourceType.MANUAL,42L));
        var snapshot=repository.findAll();
        assertThrows(UnsupportedOperationException.class,()->snapshot.clear());
        repository.add(new CollectionItem(null,"수동",1L,SourceType.MANUAL,null,null));
        assertEquals(1,snapshot.size());
        assertEquals(2,repository.findAll().size());
    }
}
