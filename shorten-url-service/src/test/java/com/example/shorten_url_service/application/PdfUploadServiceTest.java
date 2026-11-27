package com.example.shorten_url_service.application;

import com.example.shorten_url_service.infrastructure.PdfFileStorage;
import com.example.shorten_url_service.domain.ShortenUrl;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PdfUploadServiceTest {

    @Test
    void removesFileWhenKeyIssuingFails() throws Exception {
        PdfFileStorage storage = mock(PdfFileStorage.class);
        SimpleShortenUrlService shortenUrlService = mock(SimpleShortenUrlService.class);
        PdfUploadService service = new PdfUploadService(storage, shortenUrlService, mock(), mock());
        MockMultipartFile file = new MockMultipartFile("file", new byte[]{1});
        RuntimeException failure = new IllegalStateException("database unavailable");

        when(storage.save(file)).thenReturn("pdf-123.pdf");
        when(shortenUrlService.generatePdfShortenUrl("pdf-123.pdf")).thenThrow(failure);

        assertSame(failure, assertThrows(RuntimeException.class, () -> service.upload(file)));
        verify(storage).delete("pdf-123.pdf");
    }

    @Test
    void issuesCancelTokenWithThePdfCode() throws Exception {
        PdfFileStorage storage = mock(PdfFileStorage.class);
        SimpleShortenUrlService shortenUrlService = mock(SimpleShortenUrlService.class);
        PdfUploadService service = new PdfUploadService(storage, shortenUrlService, mock(), mock());
        MockMultipartFile file = new MockMultipartFile("file", new byte[]{1});
        String fileName = "pdf-123.pdf";
        String token = "12345678-1234-1234-1234-123456789abc";

        when(storage.save(file)).thenReturn(fileName);
        when(storage.createCancelToken(fileName)).thenReturn(token);
        when(shortenUrlService.generatePdfShortenUrl(fileName))
                .thenReturn(new ShortenUrl("/pdf/" + fileName, "01234567"));

        PdfUploadResult result = service.upload(file);

        assertEquals("01234567", result.shortenUrl().getShortenUrlKey());
        assertEquals("/pdf/" + fileName, result.shortenUrl().getOriginalUrl());
        assertEquals(token, result.cancelToken());
    }

    @Test
    void removesEarlierFilesWhenAnotherFileFails() throws Exception {
        PdfFileStorage storage = mock();
        SimpleShortenUrlService links = mock();
        PdfUploadService service = new PdfUploadService(storage, links, mock(), mock());
        var first = new MockMultipartFile("file", "a.pdf", "application/pdf", new byte[]{1});
        var second = new MockMultipartFile("file", "b.pdf", "application/pdf", new byte[]{2});
        when(storage.save(first)).thenReturn("pdf-1.pdf");
        when(storage.save(second)).thenThrow(new java.io.IOException("write failed"));

        assertThrows(java.io.IOException.class, () -> service.uploadBundle(java.util.List.of(first, second)));
        verify(storage).delete("pdf-1.pdf");
        verifyNoInteractions(links);
    }

    @Test
    void removesEveryFileWhenBundleRegistrationFails() throws Exception {
        PdfFileStorage storage = mock();
        SimpleShortenUrlService links = mock();
        PdfUploadService service = new PdfUploadService(storage, links, mock(), mock());
        var first = new MockMultipartFile("file", "a.pdf", "application/pdf", new byte[]{1});
        var second = new MockMultipartFile("file", "b.pdf", "application/pdf", new byte[]{2});
        when(storage.save(first)).thenReturn("pdf-1.pdf");
        when(storage.save(second)).thenReturn("pdf-2.pdf");
        when(links.generatePdfBundle(anyList())).thenThrow(new IllegalStateException("database unavailable"));

        assertThrows(IllegalStateException.class, () -> service.uploadBundle(java.util.List.of(first, second)));
        verify(storage).delete("pdf-1.pdf");
        verify(storage).delete("pdf-2.pdf");
    }
}
