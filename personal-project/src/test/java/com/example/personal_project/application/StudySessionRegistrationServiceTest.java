package com.example.personal_project.application;

import com.example.personal_project.domain.StudySession;
import com.example.personal_project.global.exception.DuplicateShortenUrlCodeException;
import com.example.personal_project.global.exception.LackOfShortenUrlKeyException;
import com.example.personal_project.global.exception.ShortLinkCodeConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudySessionRegistrationServiceTest {
    private final StudySessionCreationService creation = mock(StudySessionCreationService.class);
    private final ShortCodeGenerator generator = mock(ShortCodeGenerator.class);
    private final StudySessionRegistrationService service = new StudySessionRegistrationService(creation, generator);
    private final StudySession session = mock(StudySession.class);

    @Test
    void customCodeIsTrimmedAndNeverGenerated() {
        when(creation.create(session, "week03")).thenReturn(42L);
        assertEquals(new StudySessionRegistrationService.Result(42L, "week03"),
                service.register(session, " week03 "));
        verifyNoInteractions(generator);
    }

    @Test
    void customCollisionIsReportedWithoutRetry() {
        when(creation.create(session, "week03")).thenThrow(new ShortLinkCodeConflictException(null));
        assertThrows(DuplicateShortenUrlCodeException.class, () -> service.register(session, "week03"));
        verify(creation).create(session, "week03");
        verifyNoInteractions(generator);
    }

    @Test
    void automaticCodeCanSucceedOnFifthAttempt() {
        when(generator.generate()).thenReturn("Ab23Cd45");
        when(creation.create(session, "Ab23Cd45"))
                .thenThrow(new ShortLinkCodeConflictException(null))
                .thenThrow(new ShortLinkCodeConflictException(null))
                .thenThrow(new ShortLinkCodeConflictException(null))
                .thenThrow(new ShortLinkCodeConflictException(null))
                .thenReturn(42L);
        assertEquals(42L, service.register(session, null).sessionId().longValue());
        verify(creation, times(5)).create(session, "Ab23Cd45");
    }

    @Test
    void automaticCollisionsStopAfterFiveAttempts() {
        when(generator.generate()).thenReturn("Ab23Cd45");
        when(creation.create(session, "Ab23Cd45")).thenThrow(new ShortLinkCodeConflictException(null));
        assertThrows(LackOfShortenUrlKeyException.class, () -> service.register(session, null));
        verify(generator, times(5)).generate();
        verify(creation, times(5)).create(session, "Ab23Cd45");
    }

    @Test
    void otherUniqueConstraintsAreNotRetried() {
        when(generator.generate()).thenReturn("Ab23Cd45");
        var failure = new DuplicateKeyException("session number conflict");
        when(creation.create(session, "Ab23Cd45")).thenThrow(failure);
        assertSame(failure, assertThrows(DuplicateKeyException.class, () -> service.register(session, null)));
        verify(generator).generate();
        verify(creation).create(session, "Ab23Cd45");
    }

    @Test
    void invalidCodeOrMissingSessionDoesNotWrite() {
        for (String code : new String[]{"", "  ", "ab", "Week03", "-week03", "week--03", "a".repeat(41)}) {
            assertThrows(IllegalArgumentException.class, () -> service.register(session, code));
        }
        assertThrows(IllegalArgumentException.class, () -> service.register(null, null));
        verifyNoInteractions(creation, generator);
    }
}
