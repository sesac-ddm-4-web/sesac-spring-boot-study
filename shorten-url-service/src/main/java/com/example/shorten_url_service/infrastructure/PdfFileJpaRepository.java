package com.example.shorten_url_service.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PdfFileJpaRepository extends JpaRepository<PdfFileEntity, Long> {

    List<PdfFileEntity> findAllByShortenUrl_OriginalUrlOrderByIdAsc(String originalUrl);

    List<PdfFileEntity> findAllByShortenUrl_ShortenUrlKeyOrderByIdAsc(String shortenUrlKey);
}