package com.work.shortenurl.controller;

import com.work.shortenurl.dto.ShortenUrlCreateRequest;
import com.work.shortenurl.service.ShortenUrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ShortenUrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShortenUrlService shortenUrlService;

    private String createKey(String originalUrl, String channel) {
        return shortenUrlService.createShortenUrl(
                new ShortenUrlCreateRequest(originalUrl, channel)).shortenUrlKey();
    }

    // ===== 생성 =====

    @Test
    void 단축URL을_생성하면_201과_Location_헤더를_반환한다() throws Exception {
        mockMvc.perform(post("/api/shorten-urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"originalUrl": "https://shop.com/1", "channel": "Instagram"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/shorten-urls/")))
                .andExpect(jsonPath("$.shortenUrlKey").value(matchesPattern("[a-zA-Z0-9]{8}")))
                .andExpect(jsonPath("$.shortenUrl").value(startsWith("http://localhost:8080/")))
                .andExpect(jsonPath("$.originalUrl").value("https://shop.com/1"))
                .andExpect(jsonPath("$.channel").value("instagram"));
    }

    @Test
    void 원본URL이_비어있으면_400_INVALID_INPUT을_반환한다() throws Exception {
        mockMvc.perform(post("/api/shorten-urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"originalUrl": "", "channel": "instagram"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[0].field").value("originalUrl"));
    }

    @Test
    void http_https가_아닌_URL이면_400_INVALID_URL을_반환한다() throws Exception {
        mockMvc.perform(post("/api/shorten-urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"originalUrl": "javascript:alert(1)", "channel": "instagram"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_URL"));
    }

    @Test
    void 이_서비스의_URL을_단축하면_400_SELF_REFERENCE_URL을_반환한다() throws Exception {
        mockMvc.perform(post("/api/shorten-urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"originalUrl": "http://localhost:8080/aBcD1234", "channel": "instagram"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SELF_REFERENCE_URL"));
    }

    @Test
    void 잘못된_JSON이면_400_INVALID_REQUEST_BODY를_반환한다() throws Exception {
        mockMvc.perform(post("/api/shorten-urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"originalUrl\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"));
    }

    // ===== 리다이렉트 =====

    @Test
    void 단축URL로_접속하면_302와_원본URL을_Location으로_반환한다() throws Exception {
        String key = createKey("https://shop.com/1", "instagram");

        mockMvc.perform(get("/" + key))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://shop.com/1"));
    }

    @Test
    void 리다이렉트하면_조회API의_클릭수가_증가한다() throws Exception {
        String key = createKey("https://shop.com/1", "instagram");

        mockMvc.perform(get("/" + key));
        mockMvc.perform(get("/" + key));

        mockMvc.perform(get("/api/shorten-urls/" + key))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redirectCount").value(2));
    }

    @Test
    void 없는_키로_접속하면_404_안내_페이지를_반환한다() throws Exception {
        mockMvc.perform(get("/zzzz9999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("존재하지 않는 링크")));
    }

    @Test
    void API로_없는_키를_조회하면_JSON_404를_반환한다() throws Exception {
        mockMvc.perform(get("/api/shorten-urls/zzzz9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SHORTEN_URL_NOT_FOUND"));
    }

    @Test
    void 키_형식이_틀리면_404_NOT_FOUND를_반환한다() throws Exception {
        mockMvc.perform(get("/abc"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // ===== 조회 =====

    @Test
    void 단축URL_정보를_조회한다() throws Exception {
        String key = createKey("https://shop.com/1", "instagram");

        mockMvc.perform(get("/api/shorten-urls/" + key))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl").value("https://shop.com/1"))
                .andExpect(jsonPath("$.channel").value("instagram"))
                .andExpect(jsonPath("$.redirectCount").value(0));
    }

    @Test
    void 원본URL로_목록을_조회하면_채널별_링크와_총_클릭수를_반환한다() throws Exception {
        String instaKey = createKey("https://shop.com/1", "instagram");
        createKey("https://shop.com/1", "tiktok");
        createKey("https://shop.com/2", "instagram");
        mockMvc.perform(get("/" + instaKey));

        mockMvc.perform(get("/api/shorten-urls").param("originalUrl", "https://shop.com/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].channel").value("instagram"))
                .andExpect(jsonPath("$.totalRedirectCount").value(1));
    }

    @Test
    void 지원하지_않는_메서드면_405를_반환한다() throws Exception {
        mockMvc.perform(put("/api/shorten-urls"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }
}