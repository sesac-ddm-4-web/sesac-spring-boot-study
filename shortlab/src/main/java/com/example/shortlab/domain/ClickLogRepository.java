package com.example.shortlab.domain;

import java.time.LocalDateTime;
import java.util.List;

public interface ClickLogRepository {

    void save(String shortenUrlKey, LocalDateTime clickedAt);

    /**
     * 접속 기록을 최신순으로 최대 limit건 반환한다. from, to, beforeId는 null이면 그 조건이 빠진다.
     *
     * @param from        이 시각 이후(포함)의 기록만
     * @param toExclusive 이 시각 이전(미포함)의 기록만
     * @param beforeId    이 번호보다 오래된 기록만. "더 보기"에서 이어서 가져올 때 쓴다.
     */
    List<ClickLog> findPage(String shortenUrlKey, LocalDateTime from, LocalDateTime toExclusive,
                            Long beforeId, int limit);
}