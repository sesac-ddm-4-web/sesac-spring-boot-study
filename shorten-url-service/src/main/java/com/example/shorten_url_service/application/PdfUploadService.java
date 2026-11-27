package com.example.shorten_url_service.application;

import com.example.shorten_url_service.domain.SavedPdfFile;
import com.example.shorten_url_service.infrastructure.PdfFileStorage;
import com.example.shorten_url_service.domain.ShortenUrl;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import com.example.shorten_url_service.infrastructure.PdfFileJpaRepository;
import com.example.shorten_url_service.infrastructure.ShortenUrlJpaRepository;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfUploadService {

    private final PdfFileJpaRepository pdfFiles;
    private final ShortenUrlJpaRepository links;
    private final PdfFileStorage pdfFileStorage;
    private final SimpleShortenUrlService simpleShortenUrlService;
    private static final int MAX_FILE_COUNT = 5;
    private static final long MAX_TOTAL_SIZE = 20L * 1024 * 1024;

    public PdfUploadService(
            PdfFileStorage pdfFileStorage,
            SimpleShortenUrlService simpleShortenUrlService,
            PdfFileJpaRepository pdfFiles, ShortenUrlJpaRepository links) {
        this.pdfFiles = pdfFiles;
        this.links = links;

        this.pdfFileStorage = pdfFileStorage;
        this.simpleShortenUrlService = simpleShortenUrlService;
    }

    private void validateUploadFiles(List<MultipartFile> files) {
        if (files.isEmpty()) {
            throw new IllegalArgumentException("PDF를 한 개 이상 선택해 주세요.");
        }

        if (files.size() > MAX_FILE_COUNT) {
            throw new IllegalArgumentException("PDF는 한 번에 최대 5개까지 보낼 수 있어요.");
        }

        long totalSize = 0;
        for (MultipartFile file : files) {
            totalSize += file.getSize();
        }

        if (totalSize > MAX_TOTAL_SIZE) {
            throw new org.springframework.web.multipart.MaxUploadSizeExceededException(MAX_TOTAL_SIZE);
        }
    }

    private void saveUploadFiles(List<MultipartFile> files, List<SavedPdfFile> savedFiles)
            throws IOException {
        validateUploadFiles(files);

        try {
            for (MultipartFile file : files) {
                String originalFileName = file.getOriginalFilename();
                if (originalFileName == null || originalFileName.isBlank()) {
                    throw new IllegalArgumentException("파일 이름이 없는 PDF는 업로드할 수 없어요.");
                }

                originalFileName = originalFileName.replace('\\', '/');
                originalFileName = originalFileName.substring(originalFileName.lastIndexOf('/') + 1);
                if (originalFileName.isBlank() || originalFileName.length() > 255) {
                    throw new IllegalArgumentException("PDF 파일 이름을 확인해 주세요.");
                }
                String storedFileName = pdfFileStorage.save(file);
                savedFiles.add(new SavedPdfFile(originalFileName, storedFileName));
            }
        } catch (IOException | RuntimeException e) {
            deleteSavedFiles(savedFiles, e);
            throw e;
        }
    }

    private void deleteSavedFiles(List<SavedPdfFile> savedFiles, Exception uploadException) {
        for (SavedPdfFile file : savedFiles) {
            try {
                pdfFileStorage.delete(file.storedFileName());
            } catch (IOException deleteException) {
                uploadException.addSuppressed(deleteException);
            }
        }
    }

    public PdfUploadResult upload(MultipartFile file) throws IOException {
        return upload(file, null);
    }

    public PdfUploadResult upload(MultipartFile file, String pin) throws IOException {
        String fileName = pdfFileStorage.save(file);

        try {
            if (pin != null) pdfFileStorage.protect(fileName, pin);
            String cancelToken = pdfFileStorage.createCancelToken(fileName);
            ShortenUrl shortenUrl = simpleShortenUrlService.generatePdfShortenUrl(fileName);
            return new PdfUploadResult(shortenUrl, cancelToken);
        } catch (IOException | RuntimeException e) {
            try {
                pdfFileStorage.delete(fileName);
            } catch (IOException deleteException) {
                e.addSuppressed(deleteException);
            }
            throw e;
        }
    }

    public PdfUploadResult uploadBundle(List<MultipartFile> files) throws IOException {
        return uploadBundle(files, null);
    }

    public PdfUploadResult uploadBundle(List<MultipartFile> files, String pin) throws IOException {
        List<SavedPdfFile> savedFiles = new ArrayList<>();
        saveUploadFiles(files, savedFiles);

        try {
            String bundleId = savedFiles.getFirst().storedFileName();
            if (pin != null) pdfFileStorage.protect(bundleId, pin);
            String cancelToken = pdfFileStorage.createCancelToken(bundleId);
            ShortenUrl shortenUrl = simpleShortenUrlService.generatePdfBundle(savedFiles);

            return new PdfUploadResult(shortenUrl, cancelToken);
        } catch (IOException | RuntimeException e) {
            deleteSavedFiles(savedFiles, e);
            throw e;
        }
    }

    public List<SavedPdfFile> listBundle(String bundleId) {
        if (pdfFileStorage.load(bundleId) == null) {
            return List.of();
        }
        return pdfFiles.findAllByShortenUrl_OriginalUrlOrderByIdAsc("/pdf-bundles/" + bundleId)
                .stream()
                .map(file -> new SavedPdfFile(file.getOriginalFileName(), file.getStoredFileName()))
                .toList();
    }

    public Resource getBundleFile(String bundleId, String fileName) {
        boolean member = listBundle(bundleId).stream()
                .anyMatch(file -> file.storedFileName().equals(fileName));
        return member ? pdfFileStorage.load(fileName) : null;
    }

    public boolean cancelBundle(String bundleId, String token) throws IOException {
        List<SavedPdfFile> members = listBundle(bundleId);
        if (members.isEmpty() || !pdfFileStorage.cancel(bundleId, token)) {
            return false;
        }
        // Removing the first file immediately closes access to the entire bundle.
        IOException cleanupFailure = null;
        for (SavedPdfFile file : members) {
            if (!file.storedFileName().equals(bundleId)) {
                try {
                    pdfFileStorage.delete(file.storedFileName());
                } catch (IOException e) {
                    if (cleanupFailure == null) {
                        cleanupFailure = new IOException("PDF 묶음 삭제 실패");
                    }
                    cleanupFailure.addSuppressed(e);
                }
            }
        }
        if (cleanupFailure != null) {
            throw cleanupFailure;
        }
        return true;
    }

    public boolean cancel(String fileName, String cancelToken) throws IOException {
        return links.existsByOriginalUrl("/pdf/" + fileName) && pdfFileStorage.cancel(fileName, cancelToken);
    }

    public Resource getPdf(String fileName) {
        return links.existsByOriginalUrl("/pdf/" + fileName) ? pdfFileStorage.load(fileName) : null;
    }
}