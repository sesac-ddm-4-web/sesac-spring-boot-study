package com.example.personal_project.application.url;

import com.example.personal_project.application.url.port.StudyLinkTargetPort;
import com.example.personal_project.domain.url.model.ShortLink;
import com.example.personal_project.domain.url.repository.ShortLinkRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudyLinkRedirectServiceTest {
    private final ShortLinkRepository links = mock(ShortLinkRepository.class);
    private final StudyLinkTargetPort targets = mock(StudyLinkTargetPort.class);
    private final StudyLinkRedirectService service = new StudyLinkRedirectService(links, targets);

    @Test
    void missingCodeDoesNotReadTargetOrIncrement() {
        assertNull(service.resolve("missing"));
        verifyNoInteractions(targets);
        verify(links, never()).incrementRedirectCount(anyString());
    }

    @Test
    void missingTargetDoesNotIncrement() {
        var link = new ShortLink(1L, "week03", null, 2L, 10);
        when(links.findByCode("week03")).thenReturn(link);
        assertNull(service.resolve("week03"));
        verify(targets).destination(link);
        verify(links, never()).incrementRedirectCount(anyString());
    }

    @Test
    void resolvesCurrentTargetBeforeIncrementing() {
        var link = new ShortLink(1L, "week03", null, 2L, 10);
        when(links.findByCode("week03")).thenReturn(link);
        when(targets.destination(link)).thenReturn("https://example.com/current");
        assertEquals("https://example.com/current", service.resolve("week03"));
        var ordered = inOrder(targets, links);
        ordered.verify(targets).destination(link);
        ordered.verify(links).incrementRedirectCount("week03");
    }
}
