package com.example.shortlab.infrastructure.clicklog;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ClickLogJpaRepository extends JpaRepository<ClickLogEntity, Long> {

    @Query("""
            select c from ClickLogEntity c
            where c.shortenUrlKey = :key
              and (:from is null or c.clickedAt >= :from)
              and (:to is null or c.clickedAt < :to)
              and (:beforeId is null or c.id < :beforeId)
            order by c.id desc
            """)
    List<ClickLogEntity> findPage(@Param("key") String key,
                                  @Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to,
                                  @Param("beforeId") Long beforeId,
                                  Pageable pageable);
}