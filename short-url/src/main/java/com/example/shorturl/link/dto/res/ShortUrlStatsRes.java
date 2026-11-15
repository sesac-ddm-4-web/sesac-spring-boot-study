package com.example.shorturl.link.dto.res;

import com.example.shorturl.link.domain.DailyVisit;

import java.time.LocalDate;
import java.util.List;

public record ShortUrlStatsRes(
        String key,
        long totalCount,
        long botCount,
        long periodCount,
        LocalDate from,
        LocalDate to,
        List<DailyVisit> daily
) {
}
