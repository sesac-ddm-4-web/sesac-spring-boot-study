package com.example.short_url.infrastructure;

import com.example.short_url.domain.ShortenUrl;
import com.example.short_url.domain.ShortenUrlRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//@Repository
public class MapShortenUrlRepository implements ShortenUrlRepository {
    private Map<String, ShortenUrl> shortenUrls = new ConcurrentHashMap<>();
    
    //@Override
    public void saveShortenUrl(ShortenUrl shortenUrl){
        shortenUrls.put(shortenUrl.getShortenUrlKey(), shortenUrl);
    }

    //@Override
    public ShortenUrl findShortenUrlByShortenUrlKey(String shortenUrlKey) {
        ShortenUrl shortenUrl = shortenUrls.get(shortenUrlKey);
        return shortenUrl;
    }
}
