package com.example.shorten_url_service.application;

import com.example.shorten_url_service.domain.LackOfShortenUrlKeyException;
import com.example.shorten_url_service.domain.NotFoundShortenUrlException;
import com.example.shorten_url_service.domain.ShortenUrl;
import com.example.shorten_url_service.domain.ShortenUrlRepository;
import com.example.shorten_url_service.infrastructure.PdfFileStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimpleShortenUrlServiceUnitTest {
    @Mock
    private ShortenUrlRepository shortenUrlRepository;
    @Mock
    private PdfFileStorage pdfFileStorage;
    @InjectMocks
    private SimpleShortenUrlService simpleShortenUrlService;
    @Test
    void 생성한_키가_모두_중복이면_예외가_발생한다() {
        String originalUrl = "https://www.hanbit.co.kr/";
        ShortenUrl existingUrl = new ShortenUrl("https://example.com/", "abc123");

        when(shortenUrlRepository.findShortenUrlByShortenUrlKey(anyString()))
                .thenReturn(existingUrl);

        assertThrows(LackOfShortenUrlKeyException.class,
                () -> simpleShortenUrlService.generateShortenUrl(originalUrl));
    }

    @Test
    void 저장_중_키가_중복되면_다시_저장한다() {
        String originalUrl = "https://www.hanbit.co.kr/";

        ShortenUrl existingUrl =
                new ShortenUrl("https://example.com/", "12345678");

        when(shortenUrlRepository.findShortenUrlByShortenUrlKey(anyString()))
                .thenReturn(null, existingUrl, null);

        doThrow(new DataIntegrityViolationException("중복 키"))
                .doNothing()
                .when(shortenUrlRepository)
                .insertShortenUrl(any(ShortenUrl.class));

        var response = simpleShortenUrlService.generateShortenUrl(originalUrl);

        verify(shortenUrlRepository, times(2))
                .insertShortenUrl(any(ShortenUrl.class));

        assertEquals(originalUrl, response.getOriginalUrl());
    }

    @Test
    void expiredPdfCannotBeLookedUpOrRedirected() {
        String key = "12345678";
        String fileName = "pdf-123.pdf";
        when(shortenUrlRepository.findShortenUrlByShortenUrlKey(key))
                .thenReturn(new ShortenUrl("/pdf/" + fileName, key));
        when(pdfFileStorage.load(fileName)).thenReturn(null);

        assertThrows(NotFoundShortenUrlException.class,
                () -> simpleShortenUrlService.getShortenUrlInformationByShortenUrlKey(key));
        assertThrows(NotFoundShortenUrlException.class,
                () -> simpleShortenUrlService.getOriginalUrlByShortenUrlKey(key));

        verify(shortenUrlRepository, never()).increaseRedirectCount(key);
    }

    @Test
    void availablePdfCanBeOpened() {
        String key = "12345678";
        String fileName = "pdf-123.pdf";
        String originalUrl = "/pdf/" + fileName;
        when(shortenUrlRepository.findShortenUrlByShortenUrlKey(key))
                .thenReturn(new ShortenUrl(originalUrl, key));
        when(pdfFileStorage.load(fileName)).thenReturn(new ByteArrayResource(new byte[0]));

        assertEquals(originalUrl, simpleShortenUrlService
                .getShortenUrlInformationByShortenUrlKey(key).getOriginalUrl());
        assertEquals(originalUrl, simpleShortenUrlService.getOriginalUrlByShortenUrlKey(key));
        verify(shortenUrlRepository).increaseRedirectCount(key);
    }

    @Test
    void ordinaryWebLinkDoesNotRequirePdfStorage() {
        String key = "87654321";
        String originalUrl = "https://example.com/";
        when(shortenUrlRepository.findShortenUrlByShortenUrlKey(key))
                .thenReturn(new ShortenUrl(originalUrl, key));

        assertEquals(originalUrl, simpleShortenUrlService.getOriginalUrlByShortenUrlKey(key));
        verifyNoInteractions(pdfFileStorage);
    }
}
