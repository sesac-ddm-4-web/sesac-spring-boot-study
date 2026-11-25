package com.kdongd.shortari.repository;

import com.kdongd.shortari.domain.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    // 단축 코드로 조회
    Optional<ShortUrl> findByShortCode(String shortCode);

    // 전체 링크 조회 (최신순)
    List<ShortUrl> findAllByOrderByCreatedAtDescIdDesc();

    // 표시 이름 중복 확인
    boolean existsByDisplayName(String displayName);
}