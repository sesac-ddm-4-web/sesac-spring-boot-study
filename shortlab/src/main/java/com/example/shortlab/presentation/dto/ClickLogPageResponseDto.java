package com.example.shortlab.presentation.dto;

import com.example.shortlab.domain.ClickLog;

import java.time.LocalDateTime;
import java.util.List;

public record ClickLogPageResponseDto(List<Item> clicks, boolean hasNext) {

    public record Item(Long id, LocalDateTime clickedAt) {
    }

    public static ClickLogPageResponseDto of(List<ClickLog> logs, boolean hasNext) {
        List<Item> items = logs.stream()
                .map(log -> new Item(log.id(), log.clickedAt()))
                .toList();
        return new ClickLogPageResponseDto(items, hasNext);
    }
}