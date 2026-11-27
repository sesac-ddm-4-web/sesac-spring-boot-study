package com.example.shorten_url_service.presentation;

import com.example.shorten_url_service.application.PdfUploadResult;
import com.example.shorten_url_service.application.PdfUploadService;
import com.example.shorten_url_service.domain.ShortenUrl;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import org.springframework.web.util.HtmlUtils;
import com.example.shorten_url_service.domain.SavedPdfFile;

@RestController
public class PdfRestController {

    private final PdfUploadService pdfUploadService;

    private final com.example.shorten_url_service.infrastructure.PdfFileStorage storage;

    public PdfRestController(PdfUploadService pdfUploadService,
            com.example.shorten_url_service.infrastructure.PdfFileStorage storage) {
        this.storage = storage;
        this.pdfUploadService = pdfUploadService;
    }

    @PostMapping(value = "/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PdfUploadResponseDto upload(
            @RequestParam("file") List<MultipartFile> files, @RequestParam("pin") String pin) throws IOException {

        try {
            if (!pin.matches("[0-9]{4}")) throw new IllegalArgumentException("숫자 4자리 PIN을 입력하세요.");
            PdfUploadResult result = files.size() == 1
                    ? pdfUploadService.upload(files.getFirst(), pin)
                    : pdfUploadService.uploadBundle(files, pin);
            ShortenUrl shortenUrl = result.shortenUrl();

            return new PdfUploadResponseDto(
                    shortenUrl.getOriginalUrl(),
                    shortenUrl.getShortenUrlKey(),
                    result.cancelToken()
            );
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/pdf-access/{name}")
    public ResponseEntity<Void> unlock(@PathVariable String name, @RequestParam(defaultValue = "") String pin,
                                       jakarta.servlet.http.HttpServletRequest request) throws IOException {
        if (storage.load(name) == null) return ResponseEntity.notFound().build();
        if (!storage.requiresPin(name)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        if (!storage.verifyPin(name, pin)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        request.getSession().setAttribute("pdf-access:" + name, Boolean.TRUE);
        return ResponseEntity.noContent().build();
    }

    private boolean allowed(String name, jakarta.servlet.http.HttpServletRequest request) {
        if (storage.load(name) == null) return true;
        var session = request.getSession(false);
        return session != null &&
                Boolean.TRUE.equals(session.getAttribute("pdf-access:" + name));
    }

    @GetMapping(value = "/pdf-bundles/{bundleId}", produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> listBundle(@PathVariable String bundleId, jakarta.servlet.http.HttpServletRequest request) {
        if (!allowed(bundleId, request)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        List<SavedPdfFile> files = pdfUploadService.listBundle(bundleId);
        if (files.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        StringBuilder page = new StringBuilder("""
                <!doctype html><html lang="ko"><head><meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>PDF 목록 · 프린트 링크</title>
                <link rel="stylesheet" href="/print/styles.css"></head>
                <body><main><h1>전송받은 PDF</h1><p>열 파일을 선택하세요.</p><ul>
                """);
        for (SavedPdfFile file : files) {
            page.append("<li><a target=\"_blank\" rel=\"noopener noreferrer\" href=\"/pdf-bundles/")
                    .append(bundleId).append("/files/").append(file.storedFileName()).append("\">")
                    .append(HtmlUtils.htmlEscape(file.originalFileName())).append("</a></li>");
        }
        page.append("</ul></main></body></html>");
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(page.toString());
    }

    @GetMapping("/pdf-bundles/{bundleId}/files/{fileName}")
    public ResponseEntity<Resource> getBundleFile(@PathVariable String bundleId,
                                                  @PathVariable String fileName, jakarta.servlet.http.HttpServletRequest request) {
        if (!allowed(bundleId, request)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return pdfResponse(pdfUploadService.getBundleFile(bundleId, fileName));
    }

    @DeleteMapping("/pdf-bundles/{bundleId}")
    public ResponseEntity<Void> cancelBundle(@PathVariable String bundleId,
            @RequestHeader("X-Cancel-Token") String token) throws IOException {
        return pdfUploadService.cancelBundle(bundleId, token)
                ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/pdf/{fileName}")
    public ResponseEntity<Void> cancel(
            @PathVariable String fileName,
            @RequestHeader("X-Cancel-Token") String cancelToken) throws IOException {

        if (!pdfUploadService.cancel(fileName, cancelToken)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pdf/{fileName}")
    public ResponseEntity<Resource> getPdf(
            @PathVariable String fileName, jakarta.servlet.http.HttpServletRequest request) {
        if (!allowed(fileName, request)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        return pdfResponse(pdfUploadService.getPdf(fileName));
    }

    private ResponseEntity<Resource> pdfResponse(Resource resource) {

        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.noStore())
                .body(resource);
    }
}