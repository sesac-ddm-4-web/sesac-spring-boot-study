package com.example.shortlab.domain;

import java.time.LocalDateTime;

public record ClickLog(Long id, LocalDateTime clickedAt) {
}