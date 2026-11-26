package com.example.shorturl.link.repository;

import com.example.shorturl.link.domain.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShortUrlJpaRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByKey(String key);

    List<ShortUrl> findAllByOrderByIdDesc();
}
