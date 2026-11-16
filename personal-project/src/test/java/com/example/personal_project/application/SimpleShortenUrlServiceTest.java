package com.example.personal_project.application;

import com.example.personal_project.domain.ShortenUrl;
import com.example.personal_project.domain.ShortenUrlRepository;
import com.example.personal_project.global.exception.DuplicateShortenUrlCodeException;
import com.example.personal_project.global.exception.LackOfShortenUrlKeyException;
import com.example.personal_project.global.exception.NotFoundShortenException;
import com.example.personal_project.infrastructure.repository.MapShortenUrlRepository;
import com.example.personal_project.presentation.dto.url.ShortenUrlCreateRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimpleShortenUrlServiceTest {

    private static final String ORIGINAL_URL = "https://example.com/slides";
    private static final String KEY = "Ab23Cd45";

    private MapShortenUrlRepository repository;
    private SimpleShortenUrlService service;

    @BeforeEach
    void setUp() {
        repository = new MapShortenUrlRepository();
        service = new SimpleShortenUrlService(repository);
    }

    @Test
    void registrationSavesOriginalUrlAndCreatesEightCharacterKey() {
        var response = service.generateShortenUrl(request());

        String key = response.getShortenUrlKey();
        assertNotNull(key);
        assertEquals(8, key.length());
        assertEquals(ORIGINAL_URL, response.getOriginalUrl());

        ShortenUrl saved = repository.findShortenUrlByShortenUrlKey(key);
        assertNotNull(saved);
        assertEquals(key, saved.getShortenUrlKey());
        assertEquals(ORIGINAL_URL, saved.getOriginalUrl());
        assertEquals(0L, saved.getRedirectCount().longValue());
    }

    @Test
    void informationLookupDoesNotIncreaseRedirectCount() {
        seed();

        var first = service.getShortenUrlInformationByShortenUrlKey(KEY);
        var second = service.getShortenUrlInformationByShortenUrlKey(KEY);

        assertEquals(ORIGINAL_URL, first.getOriginalUrl());
        assertEquals(KEY, first.getShortenUrlKey());
        assertEquals(0L, first.getRedirectCount().longValue());
        assertEquals(0L, second.getRedirectCount().longValue());
        assertEquals(0L, repository.findShortenUrlByShortenUrlKey(KEY)
                .getRedirectCount().longValue());
    }

    @Test
    void originalUrlLookupIncreasesRedirectCountEachTime() {
        seed();

        assertEquals(ORIGINAL_URL, service.getOriginalUrlByShortenUrlKey(KEY));
        assertEquals(1L, service.getShortenUrlInformationByShortenUrlKey(KEY)
                .getRedirectCount().longValue());

        assertEquals(ORIGINAL_URL, service.getOriginalUrlByShortenUrlKey(KEY));
        assertEquals(2L, service.getShortenUrlInformationByShortenUrlKey(KEY)
                .getRedirectCount().longValue());
    }

    @Test
    void missingKeyThrowsNotFoundException() {
        assertThrows(NotFoundShortenException.class,
                () -> service.getShortenUrlInformationByShortenUrlKey("missing"));
        assertThrows(NotFoundShortenException.class,
                () -> service.getOriginalUrlByShortenUrlKey("missing"));
    }

    @Test
    void fiveCollisionsFailAfterFiveInsertAttempts() {
        ShortenUrlRepository occupiedRepository = mock(ShortenUrlRepository.class);
        when(occupiedRepository.saveShortenUrlIfAbsent(any(ShortenUrl.class)))
                .thenReturn(false);

        var collisionService = new SimpleShortenUrlService(occupiedRepository);

        assertThrows(LackOfShortenUrlKeyException.class,
                () -> collisionService.generateShortenUrl(request()));

        verify(occupiedRepository, times(5))
                .saveShortenUrlIfAbsent(any(ShortenUrl.class));
    }

    @Test
    void registrationReturnsSuccessfullySavedCandidateAfterOneCollision() {
        ShortenUrlRepository retryRepository = mock(ShortenUrlRepository.class);
        when(retryRepository.saveShortenUrlIfAbsent(any(ShortenUrl.class)))
                .thenReturn(false, true);
        var retryService = new SimpleShortenUrlService(retryRepository);

        var response = retryService.generateShortenUrl(request());

        var candidates = ArgumentCaptor.forClass(ShortenUrl.class);
        verify(retryRepository, times(2))
                .saveShortenUrlIfAbsent(candidates.capture());
        ShortenUrl savedCandidate = candidates.getAllValues().getLast();
        assertEquals(savedCandidate.getShortenUrlKey(), response.getShortenUrlKey());
        assertEquals(ORIGINAL_URL, savedCandidate.getOriginalUrl());
        assertEquals(ORIGINAL_URL, response.getOriginalUrl());
    }

    @Test
    void registrationCanSucceedOnFifthInsertAttempt() {
        ShortenUrlRepository retryRepository = mock(ShortenUrlRepository.class);
        when(retryRepository.saveShortenUrlIfAbsent(any(ShortenUrl.class)))
                .thenReturn(false, false, false, false, true);
        var retryService = new SimpleShortenUrlService(retryRepository);

        var response = retryService.generateShortenUrl(request());

        var candidates = ArgumentCaptor.forClass(ShortenUrl.class);
        verify(retryRepository, times(5))
                .saveShortenUrlIfAbsent(candidates.capture());
        assertEquals(candidates.getAllValues().getLast().getShortenUrlKey(),
                response.getShortenUrlKey());
        assertEquals(ORIGINAL_URL, response.getOriginalUrl());
    }

    @ParameterizedTest
    @MethodSource("validCustomCodes")
    void customCodeIsStoredAndReturnedAsRequested(String code) {
        var response = service.generateShortenUrl(request(ORIGINAL_URL, code));

        assertEquals(code, response.getShortenUrlKey());
        assertEquals(ORIGINAL_URL, response.getOriginalUrl());
        ShortenUrl saved = repository.findShortenUrlByShortenUrlKey(code);
        assertNotNull(saved);
        assertEquals(ORIGINAL_URL, saved.getOriginalUrl());
        assertEquals(0L, saved.getRedirectCount().longValue());
    }

    @Test
    void customCodeIsTrimmedBeforeStorageAndResponse() {
        var response = service.generateShortenUrl(request(ORIGINAL_URL, "  week03-jpa  "));

        assertEquals("week03-jpa", response.getShortenUrlKey());
        assertNotNull(repository.findShortenUrlByShortenUrlKey("week03-jpa"));
        assertNull(repository.findShortenUrlByShortenUrlKey("  week03-jpa  "));
    }

    @Test
    void duplicateCustomCodePreservesOriginalUrlAndRedirectCount() {
        String code = "week03-jpa";
        service.generateShortenUrl(request(ORIGINAL_URL, code));
        service.getOriginalUrlByShortenUrlKey(code);

        assertThrows(DuplicateShortenUrlCodeException.class,
                () -> service.generateShortenUrl(
                        request("https://example.com/replacement", code)));

        var information = service.getShortenUrlInformationByShortenUrlKey(code);
        assertEquals(ORIGINAL_URL, information.getOriginalUrl());
        assertEquals(1L, information.getRedirectCount().longValue());
    }

    @ParameterizedTest
    @MethodSource("invalidCustomCodes")
    void invalidCustomCodeIsRejectedWithoutSaving(String code) {
        ShortenUrlRepository guardedRepository = mock(ShortenUrlRepository.class);
        var guardedService = new SimpleShortenUrlService(guardedRepository);

        assertThrows(IllegalArgumentException.class,
                () -> guardedService.generateShortenUrl(request(ORIGINAL_URL, code)));

        verify(guardedRepository, never()).saveShortenUrlIfAbsent(any(ShortenUrl.class));
    }

    @ParameterizedTest
    @MethodSource("validOriginalUrlRequests")
    void validOriginalUrlIsPreservedForAutomaticAndCustomCodes(String originalUrl, String code) {
        var response = service.generateShortenUrl(request(originalUrl, code));

        assertEquals(originalUrl, response.getOriginalUrl());
        if (code != null) {
            assertEquals(code, response.getShortenUrlKey());
        }
        ShortenUrl saved = repository.findShortenUrlByShortenUrlKey(response.getShortenUrlKey());
        assertNotNull(saved);
        assertEquals(originalUrl, saved.getOriginalUrl());
    }

    @ParameterizedTest
    @MethodSource("invalidOriginalUrlRequests")
    void invalidOriginalUrlIsRejectedBeforeEitherRegistrationPath(String originalUrl, String code) {
        ShortenUrlRepository guardedRepository = mock(ShortenUrlRepository.class);
        var guardedService = new SimpleShortenUrlService(guardedRepository);

        assertThrows(IllegalArgumentException.class,
                () -> guardedService.generateShortenUrl(request(originalUrl, code)));

        verify(guardedRepository, never()).saveShortenUrlIfAbsent(any(ShortenUrl.class));
    }

    private static Stream<Arguments> validOriginalUrlRequests() {
        String prefix = "https://example.com/";
        return Stream.of(
                        "https://example.technology/slides",
                        "http://example.com/slides?week=3#summary",
                        "HTTPS://example.com/slides",
                        "https://example.com:8443/slides",
                        "https://example.com:65535/slides",
                        "https://example.com/a%20b?q=spring%20boot#part-1",
                        prefix + "a".repeat(2048 - prefix.length()))
                .flatMap(url -> Stream.of(Arguments.of(url, null), Arguments.of(url, "url-check")));
    }

    private static Stream<Arguments> invalidOriginalUrlRequests() {
        String prefix = "https://example.com/";
        return Stream.of(
                        null, "", "   ", prefix + "a".repeat(2049 - prefix.length()),
                        "ftp://example.com/slides", "javascript:alert(1)", "/slides/week03",
                        "https:///slides", "https://user:password@example.com/slides",
                        "https://@example.com/slides", "https://example.com:65536/slides",
                        "https://example.com:70000/slides", "https://example.com/has space",
                        "https://example.com/%ZZ", "http://", "//example.com/slides")
                .flatMap(url -> Stream.of(Arguments.of(url, null), Arguments.of(url, "url-check")));
    }

    private static Stream<String> validCustomCodes() {
        return Stream.of("abc", "123", "week03-jpa", "a".repeat(40));
    }

    private static Stream<String> invalidCustomCodes() {
        return Stream.of("", "   ", "ab", "a".repeat(41), "Week03", "-week03",
                "week03-", "week--03", "week/03", "week_03", "회차03", "week 03");
    }

    private void seed() {
        assertTrue(repository.saveShortenUrlIfAbsent(new ShortenUrl(ORIGINAL_URL, KEY)));
    }

    private ShortenUrlCreateRequestDto request() {
        return request(ORIGINAL_URL, null);
    }

    private ShortenUrlCreateRequestDto request(String originalUrl, String code) {
        var request = mock(ShortenUrlCreateRequestDto.class);
        when(request.getOriginalUrl()).thenReturn(originalUrl);
        when(request.getCode()).thenReturn(code);
        return request;
    }
}
