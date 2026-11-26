package com.example.shorturl.link.service;

import com.example.shorturl.global.exception.ErrorCode;
import com.example.shorturl.link.domain.LinkStatus;
import com.example.shorturl.link.dto.req.ShortUrlCreateReq;
import com.example.shorturl.link.dto.res.ShortUrlRes;
import com.example.shorturl.link.dto.res.ShortUrlStatsRes;
import com.example.shorturl.support.MemoryShortUrlRepository;
import com.example.shorturl.support.MutableClock;
import com.example.shorturl.support.SequenceKeyGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.example.shorturl.support.ErrorCodeAssertions.assertErrorCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShortUrlServiceTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 11, 14, 10, 0);
    private static final String URL = "https://github.com/daun/portfolio";
    private static final URI SERVICE = URI.create("http://localhost:8080");
    private static final String CHROME = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36";
    private static final String KAKAO_PREVIEW = "facebookexternalhit/1.1; kakaotalk-scrap/1.0";

    private final MemoryShortUrlRepository repository = new MemoryShortUrlRepository();
    private final MutableClock clock = new MutableClock(START);

    private ShortUrlService service(SequenceKeyGenerator generator) {
        return new ShortUrlService(repository, generator, clock);
    }

    private ShortUrlCreateReq request(String url) {
        return new ShortUrlCreateReq(url, null, null, null);
    }

    @Test
    @DisplayName("원본 URL을 등록하면 8자 키가 만들어지고, 그 키로 이동하면 원본 주소와 방문 수 1이 남는다.")
    void createAndRedirect() {
        ShortUrlService service = service(new SequenceKeyGenerator("Abcd2345"));

        ShortUrlRes created = service.create(request(URL), SERVICE);
        String target = service.redirect(created.key(), CHROME, false);

        assertEquals("Abcd2345", created.key());
        assertEquals("/Abcd2345", created.shortPath());
        assertEquals(URL, target);
        assertEquals(1, service.findByKey("Abcd2345").visitCount());
    }

    @Test
    @DisplayName("같은 원본 URL을 다시 등록하면 새 키가 만들어지고, 이전 키도 계속 동작한다.")
    void sameUrlCreatesNewKey() {
        ShortUrlService service = service(new SequenceKeyGenerator("firstKey", "secondKy"));

        ShortUrlRes first = service.create(request(URL), SERVICE);
        ShortUrlRes second = service.create(request(URL), SERVICE);

        assertNotEquals(first.key(), second.key());
        assertEquals(URL, service.redirect(first.key(), CHROME, false));
        assertEquals(URL, service.redirect(second.key(), CHROME, false));
    }

    @Test
    @DisplayName("생성한 키가 이미 있으면 다시 만들고, 기존 링크는 덮어쓰지 않는다.")
    void retryWhenKeyCollides() {
        SequenceKeyGenerator generator = new SequenceKeyGenerator("sameKey1", "sameKey1", "otherKey");
        ShortUrlService service = service(generator);
        service.create(request("https://first.com"), SERVICE);

        ShortUrlRes created = service.create(request("https://second.com"), SERVICE);

        assertEquals("otherKey", created.key());
        assertEquals(3, generator.getGeneratedCount());
        assertEquals("https://first.com", service.redirect("sameKey1", CHROME, false));
    }

    @Test
    @DisplayName("5번 모두 키가 겹치면 LACK_OF_SHORT_URL_KEY가 발생하고 저장된 링크 수는 그대로다.")
    void lackOfKey() {
        SequenceKeyGenerator generator = new SequenceKeyGenerator("sameKey1");
        ShortUrlService service = service(generator);
        service.create(request("https://first.com"), SERVICE);

        assertErrorCode(ErrorCode.LACK_OF_SHORT_URL_KEY, () -> service.create(request("https://second.com"), SERVICE));
        assertEquals(1 + ShortUrlService.MAX_KEY_RETRY, generator.getGeneratedCount());
        assertEquals(1, service.search(null, null, null).size());
    }

    @Test
    @DisplayName("원하는 짧은 주소로 등록할 수 있고, 이미 쓰는 주소면 DUPLICATE_CUSTOM_KEY가 발생한다.")
    void customKey() {
        ShortUrlService service = service(new SequenceKeyGenerator("Abcd2345"));

        ShortUrlRes created = service.create(new ShortUrlCreateReq(URL, " portfolio ", "포트폴리오", null), SERVICE);

        assertEquals("portfolio", created.key());
        assertTrue(created.custom());
        assertErrorCode(ErrorCode.DUPLICATE_CUSTOM_KEY,
                () -> service.create(new ShortUrlCreateReq("https://other.com", "portfolio", null, null), SERVICE));
        assertEquals(URL, service.redirect("portfolio", CHROME, false));
    }

    @Test
    @DisplayName("http/https가 아닌 주소, 예약어, 지난 만료일은 각각의 ErrorCode가 나오고 아무것도 저장되지 않는다.")
    void rejectInvalidRequest() {
        ShortUrlService service = service(new SequenceKeyGenerator("Abcd2345"));

        assertErrorCode(ErrorCode.UNSUPPORTED_URL_SCHEME, () -> service.create(request("javascript:alert(1)"), SERVICE));
        assertErrorCode(ErrorCode.RESERVED_CUSTOM_KEY, () -> service.create(new ShortUrlCreateReq(URL, "admin", null, null), SERVICE));
        assertErrorCode(ErrorCode.PAST_EXPIRATION_DATE,
                () -> service.create(new ShortUrlCreateReq(URL, null, null, START.toLocalDate().minusDays(1)), SERVICE));

        assertEquals(0, service.search(null, null, null).size());
    }

    @Test
    @DisplayName("없는 키로 이동하거나 조회하면 SHORT_URL_NOT_FOUND가 발생한다.")
    void notFound() {
        ShortUrlService service = service(new SequenceKeyGenerator("Abcd2345"));

        assertErrorCode(ErrorCode.SHORT_URL_NOT_FOUND, () -> service.redirect("nothing1", CHROME, false));
        assertErrorCode(ErrorCode.SHORT_URL_NOT_FOUND, () -> service.findByKey("nothing1"));
    }

    @Test
    @DisplayName("만료일이 지나면 이동할 수 없고 상태가 EXPIRED로 바뀐다.")
    void expiration() {
        ShortUrlService service = service(new SequenceKeyGenerator("Abcd2345"));
        service.create(new ShortUrlCreateReq(URL, null, null, START.toLocalDate()), SERVICE);

        clock.plusDays(1);

        assertErrorCode(ErrorCode.EXPIRED_SHORT_URL, () -> service.redirect("Abcd2345", CHROME, false));
        assertEquals(LinkStatus.EXPIRED, service.findByKey("Abcd2345").status());
    }

    @Test
    @DisplayName("최근 N일 통계는 오늘을 포함해 날짜별 방문 수를 0까지 채워서 돌려준다.")
    void stats() {
        ShortUrlService service = service(new SequenceKeyGenerator("Abcd2345"));
        service.create(request(URL), SERVICE);
        service.redirect("Abcd2345", CHROME, false);
        clock.plusDays(2);
        service.redirect("Abcd2345", CHROME, false);
        service.redirect("Abcd2345", CHROME, false);

        ShortUrlStatsRes stats = service.stats("Abcd2345", 3);

        assertEquals(3, stats.totalCount());
        assertEquals(3, stats.periodCount());
        assertEquals(LocalDate.of(2026, 11, 14), stats.from());
        assertEquals(List.of(1L, 0L, 2L), stats.daily().stream().map(d -> d.count()).toList());
        assertErrorCode(ErrorCode.INVALID_STATS_DAYS, () -> service.stats("Abcd2345", 0));
        assertErrorCode(ErrorCode.INVALID_STATS_DAYS, () -> service.stats("Abcd2345", 31));
    }

    @Test
    @DisplayName("목록은 최신순이고, 검색어와 상태로 거를 수 있다.")
    void search() {
        ShortUrlService service = service(new SequenceKeyGenerator("key00001", "key00002", "key00003"));
        service.create(new ShortUrlCreateReq("https://github.com/daun", null, "깃허브", null), SERVICE);
        service.create(new ShortUrlCreateReq("https://velog.io/@daun", null, "블로그", null), SERVICE);
        service.create(new ShortUrlCreateReq("https://notion.so/resume", null, "이력서", null), SERVICE);
        service.changeActive("key00002", false);

        assertEquals(List.of("key00003", "key00002", "key00001"),
                service.search(null, null, null).stream().map(ShortUrlRes::key).toList());
        assertEquals(List.of("key00001"), service.search("깃허브", null, null).stream().map(ShortUrlRes::key).toList());
        assertEquals(List.of("key00002"),
                service.search(null, LinkStatus.DISABLED, null).stream().map(ShortUrlRes::key).toList());
    }

    @Test
    @DisplayName("미리보기 봇은 이동은 되지만 사람 방문으로 세지 않고, 열람 여부로 거를 수 있다.")
    void botVisitAndOpenedFilter() {
        ShortUrlService service = service(new SequenceKeyGenerator("toss0001", "kakao001"));
        service.create(request(URL), SERVICE);
        service.create(request(URL), SERVICE);

        assertEquals(URL, service.redirect("toss0001", KAKAO_PREVIEW, false));
        service.redirect("toss0001", CHROME, false);
        service.redirect("kakao001", KAKAO_PREVIEW, false);

        ShortUrlRes toss = service.findByKey("toss0001");
        assertEquals(1, toss.visitCount());
        assertEquals(1, toss.botVisitCount());
        assertEquals(START, toss.firstVisitedAt());
        assertEquals(1, service.stats("toss0001", 7).botCount());

        assertEquals(List.of("toss0001"),
                service.search(null, null, true).stream().map(ShortUrlRes::key).toList());
        assertEquals(List.of("kakao001"),
                service.search(null, null, false).stream().map(ShortUrlRes::key).toList());
    }

    @Test
    @DisplayName("포트링크 자신의 주소를 원본으로 등록하면 SELF_REFERENCING_URL이고 아무것도 저장되지 않는다.")
    void rejectSelfLink() {
        ShortUrlService service = service(new SequenceKeyGenerator("Abcd2345"));

        assertErrorCode(ErrorCode.SELF_REFERENCING_URL,
                () -> service.create(new ShortUrlCreateReq("http://localhost:8080/loop-self", "loop-self", null, null), SERVICE));
        assertErrorCode(ErrorCode.SELF_REFERENCING_URL,
                () -> service.create(request("http://LOCALHOST:8080/Abcd2345"), SERVICE));

        assertEquals(0, service.search(null, null, null).size());
    }

    @Test
    @DisplayName("HEAD 요청은 브라우저 User-Agent여도 봇 방문으로 센다.")
    void headRequestIsBot() {
        ShortUrlService service = service(new SequenceKeyGenerator("Abcd2345"));
        service.create(request(URL), SERVICE);

        assertEquals(URL, service.redirect("Abcd2345", CHROME, true));

        ShortUrlRes res = service.findByKey("Abcd2345");
        assertEquals(0, res.visitCount());
        assertEquals(1, res.botVisitCount());
    }
}
