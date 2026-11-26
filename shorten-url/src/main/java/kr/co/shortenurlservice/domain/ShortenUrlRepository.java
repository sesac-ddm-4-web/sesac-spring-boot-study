
package kr.co.shortenurlservice.domain;

import java.util.List;

public interface ShortenUrlRepository {

    void saveShortenUrl(ShortenUrl shortenUrl);

    ShortenUrl findShortenUrlByShortenUrlKey(
            String shortenUrlKey
    );

    List<ShortenUrl> findAll(
            String keyword,
            int limit,
            int offset
    );

    long count(String keyword);

    void updateStatus(
            String shortenUrlKey,
            boolean active
    );

    boolean increaseRedirectCount(String shortenUrlKey);

    // 원본 URL 변경
    boolean updateOriginalUrl(
            String shortenUrlKey,
            String originalUrl
    );
}
