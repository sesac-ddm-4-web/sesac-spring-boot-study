package com.example.shorturl.link.repository;

import com.example.shorturl.link.domain.ShortUrl;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaShortUrlRepository implements ShortUrlRepository {

    private final ShortUrlJpaRepository shortUrlJpaRepository;

    public JpaShortUrlRepository(ShortUrlJpaRepository shortUrlJpaRepository) {
        this.shortUrlJpaRepository = shortUrlJpaRepository;
    }

    @Override
    public boolean saveIfAbsent(ShortUrl shortUrl) {
        try {
            shortUrlJpaRepository.saveAndFlush(shortUrl);
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }

    @Override
    public Optional<ShortUrl> findByKey(String key) {
        return shortUrlJpaRepository.findByKey(key);
    }

    @Override
    public Optional<ShortUrl> findByKeyForUpdate(String key) {
        return shortUrlJpaRepository.findWithLockByKey(key);
    }

    @Override
    public List<ShortUrl> findAll() {
        return shortUrlJpaRepository.findAllByOrderByIdDesc();
    }
}
