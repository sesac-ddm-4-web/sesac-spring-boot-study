package com.example.shorten_url_service.application;

import com.example.shorten_url_service.domain.*;
import com.example.shorten_url_service.infrastructure.PdfFileStorage;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SimpleShortenUrlService {
    private final ShortenUrlRepository shortenUrlRepository;
    private final PdfFileStorage pdfFileStorage;

    // 서비스를 만들 때 ShortenUrlRepository를 구현한 객체를 넣어줘
    SimpleShortenUrlService(ShortenUrlRepository shortenUrlRepository, PdfFileStorage pdfFileStorage) {
        this.shortenUrlRepository = shortenUrlRepository;
        this.pdfFileStorage = pdfFileStorage;
    }

    public ShortenUrl generateShortenUrl(
            String originalUrl) {

        ShortenUrl shortenUrl = createAndSaveShortenUrl(originalUrl);

        return shortenUrl;
    }

    public ShortenUrl generatePdfShortenUrl(String fileName) {
        String originalUrl = "/pdf/" + fileName;
        return createAndSaveShortenUrl(originalUrl);
    }

    private ShortenUrl createAndSaveShortenUrl(String originalUrl) {
        return createAndSaveShortenUrl(originalUrl, List.of());
    }

    public ShortenUrl generatePdfBundle(List<SavedPdfFile> files) {
        if (files.isEmpty()) {
            throw new IllegalArgumentException("PDF를 한 개 이상 선택해 주세요.");
        }

        String bundleUrl = "/pdf-bundles/" + files.getFirst().storedFileName();
        return createAndSaveShortenUrl(bundleUrl, files);
    }

    private ShortenUrl createAndSaveShortenUrl(String originalUrl, List<SavedPdfFile> files) {
        final int MAX_RETRY_COUNT = 5;

        for (int count = 0; count < MAX_RETRY_COUNT; count++) {
            String shortenUrlKey = ShortenUrl.generateShortenUrlKey();

            ShortenUrl existingUrl =
                    shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

            if (existingUrl != null) {
                continue;
            }

            ShortenUrl shortenUrl = new ShortenUrl(originalUrl, shortenUrlKey);

            try {
                if (files.isEmpty()) {
                    shortenUrlRepository.insertShortenUrl(shortenUrl);
                } else {
                    shortenUrlRepository.insertPdfBundle(shortenUrl, files);
                }
                return shortenUrl; // 저장 성공
            } catch (DataIntegrityViolationException ex) {
                // 저장 실패 후 다시 조회했더니 키가 없음
                if (shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey) == null) {
                    throw ex;
                } // 저장 실패 후 다시 조회했더니 같은 키가 있음
            }
        }
        // 마지막 시도까지 키가 계속 중복되면
        throw new LackOfShortenUrlKeyException();
    }

    private ShortenUrl findAvailableShortenUrl(String shortenUrlKey) {
        ShortenUrl shortenUrl =
                shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

        if (shortenUrl == null) {
            throw new NotFoundShortenUrlException();
        }

        String originalUrl = shortenUrl.getOriginalUrl();

        String fileName;

        if (originalUrl.startsWith("/pdf/")) {
            fileName = originalUrl.substring("/pdf/".length());
        } else if (originalUrl.startsWith("/pdf-bundles/")) {
            fileName = originalUrl.substring("/pdf-bundles/".length());
        } else {
            return shortenUrl;
        }

        if (pdfFileStorage.load(fileName) == null) {
            throw new NotFoundShortenUrlException();
        }

        return shortenUrl;
    }
    
    // 단축 URL 정보만 조회
    @Transactional(readOnly = true)
    public ShortenUrl getShortenUrlInformationByShortenUrlKey(String shortenUrlKey) {
        return findAvailableShortenUrl(shortenUrlKey);
    }

    // 링크 조회
    @Transactional
    public String getOriginalUrlByShortenUrlKey(String shortenUrlKey) {
        ShortenUrl shortenUrl = findAvailableShortenUrl(shortenUrlKey);

        shortenUrlRepository.increaseRedirectCount(shortenUrlKey);

        return shortenUrl.getOriginalUrl();
    }
}
