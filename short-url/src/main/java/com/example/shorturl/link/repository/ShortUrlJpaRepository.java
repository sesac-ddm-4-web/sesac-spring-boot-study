package com.example.shorturl.link.repository;

import com.example.shorturl.link.domain.ShortUrl;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface ShortUrlJpaRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByKey(String key);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ShortUrl> findWithLockByKey(String key);

    List<ShortUrl> findAllByOrderByIdDesc();
}
