package com.example.shorten_url_service.infrastructure;

import com.example.shorten_url_service.domain.SavedPdfFile;
import com.example.shorten_url_service.domain.ShortenUrl;
import com.example.shorten_url_service.domain.ShortenUrlRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class DatabaseShortenUrlRepository implements ShortenUrlRepository {

    private final ShortenUrlJpaRepository shortenUrlJpaRepository;
    private final PdfFileJpaRepository pdfFileJpaRepository;

    public DatabaseShortenUrlRepository(
            ShortenUrlJpaRepository shortenUrlJpaRepository,
            PdfFileJpaRepository pdfFileJpaRepository) {
        this.shortenUrlJpaRepository = shortenUrlJpaRepository;
        this.pdfFileJpaRepository = pdfFileJpaRepository;
    }

    // 저장
    @Override
    @Transactional
    public void insertShortenUrl(ShortenUrl shortenUrl) {
        ShortenUrlEntity entity = new ShortenUrlEntity(
                shortenUrl.getOriginalUrl(),
                shortenUrl.getShortenUrlKey(),
                shortenUrl.getRedirectCount()
        );

        shortenUrlJpaRepository.save(entity);
    }

    // 조회
    @Override
    public ShortenUrl findShortenUrlByShortenUrlKey(String shortenUrlKey) {
        Optional<ShortenUrlEntity> result =
                shortenUrlJpaRepository.findByShortenUrlKey(shortenUrlKey);

        if (result.isEmpty()) {
            return null;
        }

        ShortenUrlEntity entity = result.get();

        return new ShortenUrl(
                entity.getOriginalUrl(),
                entity.getShortenUrlKey(),
                entity.getRedirectCount()
        );
    }

    // 이동 횟수 증가
    @Override
    public void increaseRedirectCount(String shortenUrlKey) {
        shortenUrlJpaRepository.increaseRedirectCount(shortenUrlKey);
    }

    @Override
    @Transactional
    public void insertPdfBundle(ShortenUrl shortenUrl, List<SavedPdfFile> files) {
        ShortenUrlEntity link = new ShortenUrlEntity(
                shortenUrl.getOriginalUrl(),
                shortenUrl.getShortenUrlKey(),
                shortenUrl.getRedirectCount()
        );
        ShortenUrlEntity savedLink = shortenUrlJpaRepository.save(link);

        for (SavedPdfFile file : files) {
            PdfFileEntity pdf = new PdfFileEntity(
                    savedLink,
                    file.originalFileName(),
                    file.storedFileName()
            );
            pdfFileJpaRepository.save(pdf);
        }
    }

}