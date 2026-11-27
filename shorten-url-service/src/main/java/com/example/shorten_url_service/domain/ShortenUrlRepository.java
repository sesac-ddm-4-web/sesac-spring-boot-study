package com.example.shorten_url_service.domain;

import java.util.List;

// 저장과 조회에 사용할 메서드 선언 인터페이스
public interface ShortenUrlRepository {
    void insertShortenUrl(ShortenUrl shortenUrl);

    ShortenUrl findShortenUrlByShortenUrlKey(String shortenUrlKey);

    void increaseRedirectCount(String shortenUrlKey);

    void insertPdfBundle(ShortenUrl shortenUrl, List<SavedPdfFile> files);
}
