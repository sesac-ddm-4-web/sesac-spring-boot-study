package io.github.tkdgh816.brevlet.presentation;

import io.github.tkdgh816.brevlet.application.ShortenUrlService;
import io.github.tkdgh816.brevlet.domain.url.ShortenUrlNotFoundException;
import io.github.tkdgh816.brevlet.domain.url.ShortenUrlUnavailableException;
import io.github.tkdgh816.brevlet.dto.url.CreateShortenUrlRequest;
import io.github.tkdgh816.brevlet.dto.url.ShortenUrlResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShortenUrlController.class)
class ShortenUrlControllerTest {
    private static final String KEY = "AbCd2345";
    private static final String ORIGINAL_URL = "https://example.com/path";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShortenUrlService service;

    private ResultActions postCreate(String body) throws Exception {
        return mockMvc.perform(post("/shorten-url")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private void givenCreated(Instant expiresAt) {
        given(service.generateShortenUrl(any(CreateShortenUrlRequest.class)))
                .willReturn(new ShortenUrlResponse(ORIGINAL_URL, KEY, 0L, expiresAt));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 365})
    @DisplayName("만료 기간 경계값(1일, 365일)은 생성된다")
    void create_withExpiresInDaysBoundary_returns201(int days) throws Exception {
        givenCreated(Instant.parse("2026-01-02T00:00:00Z"));

        postCreate("""
                {"originalUrl":"%s","expiresInDays":%d}
                """.formatted(ORIGINAL_URL, days))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 366, -1})
    @DisplayName("만료 기간이 1~365일을 벗어나면 400이다")
    void create_withOutOfRangeExpiresInDays_returns400(int days) throws Exception {
        postCreate("""
                {"originalUrl":"%s","expiresInDays":%d}
                """.formatted(ORIGINAL_URL, days))
                .andExpect(status().isBadRequest());

        verify(service, never()).generateShortenUrl(any());
    }

    @Test
    @DisplayName("만료 기간이 숫자가 아니면 400이다")
    void create_withNonNumericExpiresInDays_returns400() throws Exception {
        postCreate("""
                {"originalUrl":"%s","expiresInDays":"abc"}
                """.formatted(ORIGINAL_URL))
                .andExpect(status().isBadRequest());

        verify(service, never()).generateShortenUrl(any());
    }

    @Test
    @DisplayName("영구 링크는 expiresAt이 null로 내려온다")
    void create_permanentLink_returnsNullExpiresAt() throws Exception {
        givenCreated(null);

        postCreate("""
                {"originalUrl":"%s"}
                """.formatted(ORIGINAL_URL))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortenUrlKey").value(KEY))
                .andExpect(jsonPath("$.expiresAt").isEmpty());
    }

    @Test
    @DisplayName("만료 시각은 ISO 8601 UTC 문자열로 내려온다")
    void create_returnsExpiresAtInIso8601() throws Exception {
        givenCreated(Instant.parse("2026-01-08T00:00:00Z"));

        postCreate("""
                {"originalUrl":"%s","expiresInDays":7}
                """.formatted(ORIGINAL_URL))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expiresAt").value("2026-01-08T00:00:00Z"));
    }

    @Test
    @DisplayName("사용 가능한 링크는 302와 원본 URL Location으로 리다이렉트된다")
    void redirect_availableLink_returns302WithLocation() throws Exception {
        given(service.getOriginalUrlByKey(KEY)).willReturn(ORIGINAL_URL);

        mockMvc.perform(get("/" + KEY))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", ORIGINAL_URL));
    }

    @Test
    @DisplayName("만료된 링크는 410이다")
    void redirect_expiredLink_returns410() throws Exception {
        given(service.getOriginalUrlByKey(KEY)).willThrow(new ShortenUrlUnavailableException("expired"));

        mockMvc.perform(get("/" + KEY))
                .andExpect(status().isGone());
    }

    @Test
    @DisplayName("존재하지 않는 키는 404이다")
    void redirect_unknownKey_returns404() throws Exception {
        given(service.getOriginalUrlByKey(KEY)).willThrow(new ShortenUrlNotFoundException("not found"));

        mockMvc.perform(get("/" + KEY))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("만료된 링크도 정보 조회는 200이고 expiresAt이 내려온다")
    void getInfo_expiredLink_returns200WithExpiresAt() throws Exception {
        given(service.getShortenUrlInfoByKey(KEY))
                .willReturn(new ShortenUrlResponse(ORIGINAL_URL, KEY, 3L, Instant.parse("2025-12-31T00:00:00Z")));

        mockMvc.perform(get("/shorten-url/" + KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redirectCount").value(3))
                .andExpect(jsonPath("$.expiresAt").value("2025-12-31T00:00:00Z"));
    }

    @Test
    @DisplayName("존재하지 않는 키의 정보 조회는 404이다")
    void getInfo_unknownKey_returns404() throws Exception {
        given(service.getShortenUrlInfoByKey(KEY)).willThrow(new ShortenUrlNotFoundException("not found"));

        mockMvc.perform(get("/shorten-url/" + KEY))
                .andExpect(status().isNotFound());
    }
}
