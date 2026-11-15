package com.example.shorturl.link.domain;

import java.time.LocalDate;

public record DailyVisit(LocalDate date, long count) {
}
