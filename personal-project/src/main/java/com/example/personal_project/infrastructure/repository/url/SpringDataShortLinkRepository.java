package com.example.personal_project.infrastructure.repository.url;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SpringDataShortLinkRepository extends JpaRepository<ShortLinkEntity, Long> {

    Optional<ShortLinkEntity> findByCode(String code);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update ShortLinkEntity link
            set link.redirectCount = link.redirectCount + 1
            where link.code = :code
            """)
    int incrementRedirectCount(@Param("code") String code);
}
