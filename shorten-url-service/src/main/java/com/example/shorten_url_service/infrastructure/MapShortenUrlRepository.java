package com.example.shorten_url_service.infrastructure;

import com.example.shorten_url_service.domain.ShortenUrl;
import com.example.shorten_url_service.domain.ShortenUrlRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// HashMap으로 저장과 조회 실제 수행 클래스
public class MapShortenUrlRepository {
    private final Map<String, ShortenUrl> shortenUrls = new ConcurrentHashMap<>();
    // 여러 요청이 동시에 하나의 저장소를 공유하기 때문에

    public void saveShortenUrl(ShortenUrl shortenUrl) {
        shortenUrls.put(shortenUrl.getShortenUrlKey(), shortenUrl);
    }

    // 주소에서 받은 단축키로 예전에 저장한 ShortenUrl 객체 찾기
    public ShortenUrl findShortenUrlByShortenUrlKey(String shortenUrlKey) {
        return shortenUrls.get(shortenUrlKey);
    }
}
