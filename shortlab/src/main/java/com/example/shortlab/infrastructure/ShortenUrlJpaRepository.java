package com.example.shortlab.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/// 이 인터페이스를 도메인 계층에 안 두는 이유
/// JpaRepository를 상속하고, @Query로 JPQL을 쓰고, 엔티티를 다룬다. -> jpa 기술에 묶임
/// 이는 어떤 DB나 프레임워크를 쓰는 지 구체적으로 명시되어 있어서
public interface ShortenUrlJpaRepository extends JpaRepository<ShortenUrlEntity, Long> {

    Optional<ShortenUrlEntity> findByShortenUrlKey(String shortenUrlKey);

    @Modifying
    @Query("update ShortenUrlEntity e set e.redirectCount = e.redirectCount + 1 " +
            "where e.shortenUrlKey = :shortenUrlKey")
    int incrementRedirectCount(@Param("shortenUrlKey") String shortenUrlKey);

    boolean existsByShortenUrlKey(String key);

}