package com.example.shorturl;

import com.example.shorturl.support.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ApiResponseTest {

    @TestConfiguration
    static class ClockTestConfig {
        @Bean
        @Primary
        MutableClock mutableClock() {
            return new MutableClock(LocalDateTime.of(2026, 11, 14, 10, 0));
        }
    }

    private static final String CHROME = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MutableClock clock;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private record Response(int status, String body, String location, String cacheControl) {
    }

    private Response call(MockHttpServletRequestBuilder request) throws Exception {
        MvcResult result = mockMvc.perform(request).andReturn();
        return new Response(
                result.getResponse().getStatus(),
                result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                result.getResponse().getHeader("Location"),
                result.getResponse().getHeader("Cache-Control"));
    }

    private Response create(String json) throws Exception {
        return call(post("/api/short-urls").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private String keyOf(Response response) {
        Matcher matcher = Pattern.compile("\"key\":\"([^\"]+)\"").matcher(response.body());
        assertTrue(matcher.find(), response.body());
        return matcher.group(1);
    }

    @Test
    @DisplayName("등록은 201과 정보 리소스 Location을, 이동은 302와 원본 Location을 돌려준다.")
    void createAndRedirect() throws Exception {
        Response created = create("{\"originalUrl\":\"https://github.com/daun\"}");
        String key = keyOf(created);

        assertEquals(201, created.status());
        assertEquals("/api/short-urls/" + key, created.location());
        assertEquals(8, key.length());

        Response redirect = call(get("/" + key).header(HttpHeaders.USER_AGENT, CHROME));
        assertEquals(302, redirect.status());
        assertEquals("https://github.com/daun", redirect.location());
        assertEquals("no-store", redirect.cacheControl());

        Response info = call(get("/api/short-urls/" + key));
        assertEquals(200, info.status());
        assertTrue(info.body().contains("\"visitCount\":1"), info.body());
    }

    @Test
    @DisplayName("잘못된 입력은 400과 사유 메시지를 돌려준다.")
    void badRequest() throws Exception {
        Response ftp = create("{\"originalUrl\":\"ftp://example.com\"}");
        Response blank = create("{\"originalUrl\":\"  \"}");
        Response broken = create("{\"originalUrl\":");
        Response badDate = create("{\"originalUrl\":\"https://a.com\",\"expiresOn\":\"2026-11-13\"}");
        Response badStatus = call(get("/api/short-urls").param("status", "UNKNOWN"));

        assertEquals(400, ftp.status());
        assertTrue(ftp.body().contains("http 또는 https"), ftp.body());
        assertEquals(400, blank.status());
        assertTrue(blank.body().contains("\"code\":\"INVALID_INPUT\""), blank.body());
        assertTrue(blank.body().contains("originalUrl"), blank.body());
        assertEquals(400, broken.status());
        assertEquals(400, badDate.status());
        assertEquals(400, badStatus.status());
        assertEquals("[]", call(get("/api/short-urls")).body());
    }

    @Test
    @DisplayName("원하는 짧은 주소가 겹치면 409, 없는 키는 404를 돌려준다.")
    void conflictAndNotFound() throws Exception {
        assertEquals(201, create("{\"originalUrl\":\"https://a.com\",\"customKey\":\"portfolio\"}").status());

        Response conflict = create("{\"originalUrl\":\"https://b.com\",\"customKey\":\"portfolio\"}");
        assertEquals(409, conflict.status());
        assertTrue(conflict.body().contains("\"code\":\"DUPLICATE_CUSTOM_KEY\""), conflict.body());
        assertEquals(302, call(get("/portfolio").header(HttpHeaders.USER_AGENT, CHROME)).status());
        assertEquals("https://a.com", call(get("/portfolio").header(HttpHeaders.USER_AGENT, CHROME)).location());

        assertEquals(404, call(get("/nothing1").header(HttpHeaders.USER_AGENT, CHROME)).status());
        assertEquals(404, call(get("/api/short-urls/nothing1")).status());
    }

    @Test
    @DisplayName("사용 중지된 링크는 403, 만료된 링크는 410을 돌려준다.")
    void disabledAndExpired() throws Exception {
        create("{\"originalUrl\":\"https://a.com\",\"customKey\":\"event-2026\",\"expiresOn\":\"2026-11-14\"}");

        Response disabled = call(patch("/api/short-urls/event-2026/status")
                .contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"));
        assertEquals(200, disabled.status());
        assertTrue(disabled.body().contains("\"status\":\"DISABLED\""), disabled.body());
        assertEquals(403, call(get("/event-2026").header(HttpHeaders.USER_AGENT, CHROME)).status());

        call(patch("/api/short-urls/event-2026/status").contentType(MediaType.APPLICATION_JSON).content("{\"active\":true}"));
        assertEquals(302, call(get("/event-2026").header(HttpHeaders.USER_AGENT, CHROME)).status());

        clock.plusDays(1);
        assertEquals(410, call(get("/event-2026").header(HttpHeaders.USER_AGENT, CHROME)).status());
    }

    @Test
    @DisplayName("통계 API는 기간 방문 수를 돌려주고 범위를 벗어난 days는 400이다.")
    void stats() throws Exception {
        create("{\"originalUrl\":\"https://a.com\",\"customKey\":\"resume\"}");
        call(get("/resume").header(HttpHeaders.USER_AGENT, CHROME));
        call(get("/resume").header(HttpHeaders.USER_AGENT, CHROME));

        Response stats = call(get("/api/short-urls/resume/stats").param("days", "7"));
        assertEquals(200, stats.status());
        assertTrue(stats.body().contains("\"periodCount\":2"), stats.body());
        assertEquals(400, call(get("/api/short-urls/resume/stats").param("days", "31")).status());
    }

    @Test
    @DisplayName("정적 화면(index.html)은 이동 주소로 오인되지 않는다.")
    void staticPageIsNotShortKey() throws Exception {
        Response page = call(get("/index.html"));
        assertEquals(200, page.status());
    }

    @Test
    @DisplayName("카카오톡 미리보기 봇도 302로 보내지만 방문 수가 아니라 봇 방문 수에 들어간다.")
    void previewBotRedirect() throws Exception {
        create("{\"originalUrl\":\"https://a.com\",\"customKey\":\"toss-github\"}");

        Response bot = call(get("/toss-github").header(HttpHeaders.USER_AGENT, "facebookexternalhit/1.1; kakaotalk-scrap/1.0"));
        Response noAgent = call(get("/toss-github"));

        assertEquals(302, bot.status());
        assertEquals("https://a.com", bot.location());
        assertEquals(302, noAgent.status());
        Response info = call(get("/api/short-urls/toss-github"));
        assertTrue(info.body().contains("\"visitCount\":0"), info.body());
        assertTrue(info.body().contains("\"botVisitCount\":2"), info.body());
        assertEquals("[]", call(get("/api/short-urls").param("opened", "true")).body());
    }

    @Test
    @DisplayName("자기 자신 주소를 등록하면 400 SELF_REFERENCING_URL, HEAD 요청은 302지만 봇 방문으로 센다.")
    void selfLinkAndHeadRequest() throws Exception {
        Response self = create("{\"originalUrl\":\"http://localhost/loop-self\",\"customKey\":\"loop-self\"}");
        assertEquals(400, self.status());
        assertTrue(self.body().contains("\"code\":\"SELF_REFERENCING_URL\""), self.body());

        create("{\"originalUrl\":\"https://a.com\",\"customKey\":\"toss-github\"}");
        Response headResponse = call(head("/toss-github").header(HttpHeaders.USER_AGENT, CHROME));
        assertEquals(302, headResponse.status());
        Response info = call(get("/api/short-urls/toss-github"));
        assertTrue(info.body().contains("\"visitCount\":0"), info.body());
        assertTrue(info.body().contains("\"botVisitCount\":1"), info.body());
    }
}
