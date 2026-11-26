package com.example.shorturl.support;

import com.example.shorturl.link.domain.ShortUrl;
import com.example.shorturl.link.repository.ShortUrlRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MemoryShortUrlRepository implements ShortUrlRepository {

    private final Map<String, ShortUrl> store = new LinkedHashMap<>();

    // 확인과 저장을 한 번에 해서 같은 키를 덮어쓰지 않게 함
    @Override
    public synchronized boolean saveIfAbsent(ShortUrl shortUrl) {
        if (store.containsKey(shortUrl.getKey())) {
            return false;
        }
        store.put(shortUrl.getKey(), shortUrl);
        return true;
    }

    @Override
    public synchronized Optional<ShortUrl> findByKey(String key) {
        return Optional.ofNullable(store.get(key));
    }

    @Override
    public Optional<ShortUrl> findByKeyForUpdate(String key) {
        return findByKey(key);
    }

    @Override
    public synchronized List<ShortUrl> findAll() {
        List<ShortUrl> newestFirst = new ArrayList<>(store.values());
        Collections.reverse(newestFirst);
        return newestFirst;
    }
}
