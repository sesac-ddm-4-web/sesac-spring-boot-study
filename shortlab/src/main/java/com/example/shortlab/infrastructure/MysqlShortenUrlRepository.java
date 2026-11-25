package com.example.shortlab.infrastructure;

import com.example.shortlab.domain.ShortenUrl;
import com.example.shortlab.domain.ShortenUrlRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class MysqlShortenUrlRepository implements ShortenUrlRepository {

    private final ShortenUrlJpaRepository jpaRepository;

    public MysqlShortenUrlRepository(ShortenUrlJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void saveShortenUrl(ShortenUrl shortenUrl) {
        jpaRepository.save(
                new ShortenUrlEntity(shortenUrl.getShortenUrlKey(), shortenUrl.getOriginalUrl()));
    }

    @Override
    public ShortenUrl findShortenUrlByShortenUrlKey(String shortenUrlKey) {
        return jpaRepository.findByShortenUrlKey(shortenUrlKey)
                .map(entity -> new ShortenUrl(
                        entity.getOriginalUrl(), entity.getShortenUrlKey(), entity.getRedirectCount()))
                .orElse(null);
    }

    @Override
    @Transactional
    public void incrementRedirectCount(String shortenUrlKey) {
        jpaRepository.incrementRedirectCount(shortenUrlKey);
    }

    @Override
    public boolean existsByShortenUrlKey(String key){
        return jpaRepository.existsByShortenUrlKey(key);
    }
}