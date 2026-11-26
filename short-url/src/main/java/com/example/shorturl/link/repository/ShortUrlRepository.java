package com.example.shorturl.link.repository;

import com.example.shorturl.link.domain.ShortUrl;

import java.util.List;
import java.util.Optional;

public interface ShortUrlRepository {

    boolean saveIfAbsent(ShortUrl shortUrl);

    Optional<ShortUrl> findByKey(String key);

    Optional<ShortUrl> findByKeyForUpdate(String key);

    List<ShortUrl> findAll();
}
