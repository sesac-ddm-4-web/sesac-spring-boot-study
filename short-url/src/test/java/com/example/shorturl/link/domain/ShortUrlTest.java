package com.example.shorturl.link.domain;

import com.example.shorturl.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.example.shorturl.support.ErrorCodeAssertions.assertErrorCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShortUrlTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 14, 10, 0);

    private ShortUrl shortUrl(LocalDate expiresOn) {
        return new ShortUrl("abcd2345", "https://example.com", "포트폴리오", false, NOW, expiresOn);
    }

    @Test
    @DisplayName("방문하면 원본 주소를 돌려주고 전체 방문 수와 날짜별 방문 수가 늘어난다.")
    void visitCountsTotalAndDaily() {
        ShortUrl shortUrl = shortUrl(null);

        assertEquals("https://example.com", shortUrl.visit(NOW, false));
        shortUrl.visit(NOW.plusHours(1), false);
        shortUrl.visit(NOW.plusDays(1), false);

        assertEquals(3, shortUrl.getVisitCount());
        assertEquals(
                List.of(new DailyVisit(NOW.toLocalDate(), 2), new DailyVisit(NOW.toLocalDate().plusDays(1), 1),
                        new DailyVisit(NOW.toLocalDate().plusDays(2), 0)),
                shortUrl.dailyVisits(NOW.toLocalDate(), NOW.toLocalDate().plusDays(2))
        );
    }

    @Test
    @DisplayName("만료일 당일까지는 이동하고 다음 날부터는 EXPIRED_SHORT_URL이 발생하며 방문 수는 그대로다.")
    void expiresAfterExpiresOn() {
        ShortUrl shortUrl = shortUrl(NOW.toLocalDate());

        shortUrl.visit(NOW.withHour(23).withMinute(59), false);
        assertEquals(LinkStatus.ACTIVE, shortUrl.status(NOW.toLocalDate()));

        assertErrorCode(ErrorCode.EXPIRED_SHORT_URL, () -> shortUrl.visit(NOW.plusDays(1).withHour(0), false));
        assertEquals(LinkStatus.EXPIRED, shortUrl.status(NOW.toLocalDate().plusDays(1)));
        assertEquals(1, shortUrl.getVisitCount());
    }

    @Test
    @DisplayName("사용 중지하면 DISABLED_SHORT_URL이 발생하고, 다시 켜면 이동한다.")
    void disableAndEnable() {
        ShortUrl shortUrl = shortUrl(null);

        shortUrl.changeActive(false);
        assertErrorCode(ErrorCode.DISABLED_SHORT_URL, () -> shortUrl.visit(NOW, false));
        assertEquals(LinkStatus.DISABLED, shortUrl.status(NOW.toLocalDate()));
        assertEquals(0, shortUrl.getVisitCount());

        shortUrl.changeActive(true);
        assertEquals("https://example.com", shortUrl.visit(NOW, false));
    }

    @Test
    @DisplayName("빈 제목은 null로 저장된다.")
    void blankTitleIsNull() {
        ShortUrl shortUrl = new ShortUrl("abcd2345", "https://example.com", "   ", false, NOW, null);
        assertNull(shortUrl.getTitle());
    }

    @Test
    @DisplayName("검색어는 키, 원본 주소, 제목에서 대소문자 구분 없이 찾는다.")
    void matchesKeyword() {
        ShortUrl shortUrl = shortUrl(null);

        assertEquals(true, shortUrl.matches("ABCD"));
        assertEquals(true, shortUrl.matches("EXAMPLE"));
        assertEquals(true, shortUrl.matches("포트"));
        assertEquals(true, shortUrl.matches(null));
        assertEquals(false, shortUrl.matches("github"));
    }

    @Test
    @DisplayName("봇 방문도 원본 주소로 보내지만 사람 방문 수·날짜별 통계·첫 열람 시각에는 들어가지 않는다.")
    void botVisitIsCountedSeparately() {
        ShortUrl shortUrl = shortUrl(null);

        assertEquals("https://example.com", shortUrl.visit(NOW, true));

        assertEquals(0, shortUrl.getVisitCount());
        assertEquals(1, shortUrl.getBotVisitCount());
        assertFalse(shortUrl.isOpened());
        assertNull(shortUrl.getFirstVisitedAt());
        assertEquals(0, shortUrl.dailyVisits(NOW.toLocalDate(), NOW.toLocalDate()).get(0).count());
    }

    @Test
    @DisplayName("첫 열람 시각은 처음 사람 방문 때만 정해지고, 마지막 열람 시각은 방문마다 바뀐다.")
    void firstAndLastVisitedAt() {
        ShortUrl shortUrl = shortUrl(null);

        shortUrl.visit(NOW, true);
        shortUrl.visit(NOW.plusHours(1), false);
        shortUrl.visit(NOW.plusHours(3), false);
        shortUrl.visit(NOW.plusHours(5), true);

        assertTrue(shortUrl.isOpened());
        assertEquals(NOW.plusHours(1), shortUrl.getFirstVisitedAt());
        assertEquals(NOW.plusHours(3), shortUrl.getLastVisitedAt());
    }
}
