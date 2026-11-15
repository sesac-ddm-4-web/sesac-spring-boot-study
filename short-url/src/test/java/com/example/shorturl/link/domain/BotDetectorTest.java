package com.example.shorturl.link.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BotDetectorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "facebookexternalhit/1.1; kakaotalk-scrap/1.0; +https://devtalk.kakao.com/",
            "Slackbot-LinkExpanding 1.0 (+https://api.slack.com/robots)",
            "Mozilla/5.0 (compatible; Discordbot/2.0; +https://discordapp.com)",
            "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
            "Mozilla/5.0 (compatible; Yeti/1.1; +http://naver.me/spd)",
            "TelegramBot (like TwitterBot)",
            "curl/8.7.1",
            "   "
    })
    @DisplayName("미리보기·검색 로봇과 명령줄 도구, 빈 User-Agent는 봇이다.")
    void bots(String userAgent) {
        assertTrue(BotDetector.isBot(userAgent));
    }

    @Test
    @DisplayName("User-Agent가 없으면 봇으로 본다.")
    void nullIsBot() {
        assertTrue(BotDetector.isBot(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.0 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36 Edg/130.0.0.0"
    })
    @DisplayName("일반 브라우저는 사람 방문이다.")
    void browsers(String userAgent) {
        assertFalse(BotDetector.isBot(userAgent));
    }
}
