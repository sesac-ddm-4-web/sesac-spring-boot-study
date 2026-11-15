package com.example.shorturl.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class MutableClock extends Clock {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private Instant instant;

    public MutableClock(LocalDateTime start) {
        this.instant = start.atZone(ZONE).toInstant();
    }

    public void plusDays(long days) {
        instant = instant.plus(Duration.ofDays(days));
    }

    @Override
    public ZoneId getZone() {
        return ZONE;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return instant;
    }
}
