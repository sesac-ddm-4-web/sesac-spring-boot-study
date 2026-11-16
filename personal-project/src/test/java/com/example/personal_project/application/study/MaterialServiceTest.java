package com.example.personal_project.application.study;

import com.example.personal_project.domain.study.exception.StudyException;
import com.example.personal_project.domain.study.model.Material;
import com.example.personal_project.domain.study.repository.MaterialRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MaterialServiceTest {
    private final MaterialRepository materials = mock(MaterialRepository.class);
    private final MaterialService service = new MaterialService(materials);

    @Test
    void anotherAuthorCannotUpdateEvenWhenCallingServiceDirectly() {
        when(materials.material(1)).thenReturn(new Material(1L, 2L, 3L, "자료", "https://example.com"));
        assertEquals(StudyException.Reason.FORBIDDEN,
                assertThrows(StudyException.class, () -> service.update(1, 4, "변경", "https://example.com/new")).reason());
        verify(materials, never()).updateMaterial(anyLong(), anyString(), anyString());
    }

    @Test
    void invalidUrlDoesNotOverwriteExistingMaterial() {
        when(materials.material(1)).thenReturn(new Material(1L, 2L, 3L, "자료", "https://example.com"));
        assertThrows(IllegalArgumentException.class, () -> service.update(1, 3, "변경", "javascript:alert(1)"));
        verify(materials, never()).updateMaterial(anyLong(), anyString(), anyString());
    }

    @Test
    void ownerCanUpdateValidatedMaterial() {
        when(materials.material(1)).thenReturn(new Material(1L, 2L, 3L, "자료", "https://example.com"));
        service.update(1, 3, "  변경  ", "https://example.com/new");
        verify(materials).updateMaterial(1, "변경", "https://example.com/new");
    }
}
