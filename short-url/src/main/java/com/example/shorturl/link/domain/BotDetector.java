package com.example.shorturl.link.domain;

import java.util.List;
import java.util.Locale;

public final class BotDetector {

    private static final List<String> BOT_KEYWORDS = List.of(
            "bot", "crawler", "spider", "kakaotalk-scrap", "facebookexternalhit", "yeti",
            "whatsapp", "preview", "headless", "curl", "wget", "python-requests", "java-http-client");

    private BotDetector() {
    }

    public static boolean isBot(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return true;
        }
        String lower = userAgent.toLowerCase(Locale.ROOT);
        return BOT_KEYWORDS.stream().anyMatch(lower::contains);
    }
}
