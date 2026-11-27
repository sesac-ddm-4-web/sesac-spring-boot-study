package com.example.shorten_url_service.application;

import com.example.shorten_url_service.domain.NotFoundShortenUrlException;
import com.example.shorten_url_service.domain.SavedPdfFile;
import com.example.shorten_url_service.domain.ShortenUrl;
import com.example.shorten_url_service.domain.ShortenUrlRepository;
import com.example.shorten_url_service.infrastructure.PdfFileJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SimpleShortenUrlServiceTest {
    @Autowired
    private SimpleShortenUrlService simpleShortenUrlService;

    @Autowired
    private ShortenUrlRepository shortenUrlRepository;

    @Autowired
    private PdfFileJpaRepository pdfFileJpaRepository;

    @Test
    @Transactional
    void PDF_두_개를_한_코드로_조회한다() {
        List<SavedPdfFile> files = List.of(
                new SavedPdfFile("이력서.pdf", "pdf-test-resume.pdf"),
                new SavedPdfFile("포트폴리오.pdf", "pdf-test-portfolio.pdf")
        );

        ShortenUrl bundle = simpleShortenUrlService.generatePdfBundle(files);

        var savedFiles = pdfFileJpaRepository
                .findAllByShortenUrl_ShortenUrlKeyOrderByIdAsc(bundle.getShortenUrlKey());

        assertEquals(2, savedFiles.size());
        assertEquals("이력서.pdf", savedFiles.get(0).getOriginalFileName());
        assertEquals("포트폴리오.pdf", savedFiles.get(1).getOriginalFileName());
    }

    // 단축 URL을 생성한 후 조회 되는지
    @Test
    @Transactional
    @DisplayName("URL을 단축한 후 단축된 URL 키로 조회하면 원래 URL이 조회되어야 한다.")
    void 단축_URL을_생성한_후_키로_조회하면_원래_URL을_반환한다() {
        String expectedOriginalUrl = "https://www.hanbit.co.kr/";
        ShortenUrl shortenUrl =
                simpleShortenUrlService.generateShortenUrl(expectedOriginalUrl);

        String shortenUrlKey = shortenUrl.getShortenUrlKey();
        assertTrue(shortenUrlKey.matches("[0-9]{8}")); // 숫자 8자리인지 확인

        String originalUrl = simpleShortenUrlService.getOriginalUrlByShortenUrlKey(shortenUrlKey);
        assertTrue(originalUrl.equals(expectedOriginalUrl));
    }

    // 존재하지 않는 단축 URL을 조회하는 경우는 여러분이 테스트 작성해 보기
    @Test
    @DisplayName("존재하지 않은 단축 URL 키로 조회하면 예외가 발생한다.")
    void 존재하지_않는_단축_URL_키로_조회하면_예외가_발생한다() {
        String missingKey = "missing-key";

        assertThrows(NotFoundShortenUrlException.class,
                () -> simpleShortenUrlService.getOriginalUrlByShortenUrlKey(missingKey));
    }

    @Test
    void 중복_키를_저장해도_기존_URL은_유지된다() {
        String originalUrl = "https://www.hanbit.co.kr/";
        var response = simpleShortenUrlService.generateShortenUrl(originalUrl);
        String shortenUrlKey = response.getShortenUrlKey();

        ShortenUrl duplicateUrl =
                new ShortenUrl("https://example.com/", shortenUrlKey);

        assertThrows(DataIntegrityViolationException.class,
                () -> shortenUrlRepository.insertShortenUrl(duplicateUrl));

        ShortenUrl savedUrl =
                shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

        assertEquals(originalUrl, savedUrl.getOriginalUrl());
    }
}

