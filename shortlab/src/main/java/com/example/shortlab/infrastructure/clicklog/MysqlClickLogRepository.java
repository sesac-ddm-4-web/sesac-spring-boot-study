package com.example.shortlab.infrastructure.clicklog;

import com.example.shortlab.domain.ClickLog;
import com.example.shortlab.domain.ClickLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class MysqlClickLogRepository implements ClickLogRepository {

    private final ClickLogJpaRepository clickLogJpaRepository;

    public MysqlClickLogRepository(ClickLogJpaRepository clickLogJpaRepository) {
        this.clickLogJpaRepository = clickLogJpaRepository;
    }

    // 항상 새 트랜잭션: 바깥 트랜잭션이 생겨도 로그 저장 실패가 카운트 증가 등을 롤백시키지 않는다.
    // (같은 클래스 내부 호출은 프록시를 안 거치므로, 반드시 서비스 -> 이 빈으로 호출해야 적용된다)
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(String shortenUrlKey, LocalDateTime clickedAt) {
        clickLogJpaRepository.save(new ClickLogEntity(shortenUrlKey, clickedAt));
    }

    @Override
    public List<ClickLog> findPage(String shortenUrlKey, LocalDateTime from, LocalDateTime toExclusive,
                                   Long beforeId, int limit) {
        return clickLogJpaRepository
                .findPage(shortenUrlKey, from, toExclusive, beforeId, PageRequest.of(0, limit))
                .stream()
                .map(entity -> new ClickLog(entity.getId(), entity.getClickedAt()))
                .toList();
    }
}