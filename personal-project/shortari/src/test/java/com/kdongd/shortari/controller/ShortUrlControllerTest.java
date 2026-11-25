package com.kdongd.shortari.controller;

import com.kdongd.shortari.domain.UrlMode;
import com.kdongd.shortari.dto.response.ShortUrlResponse;
import com.kdongd.shortari.exception.ShortUrlNotFoundException;
import com.kdongd.shortari.service.ShortUrlService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ShortUrlControllerTest {

    @Mock
    private ShortUrlService service;

    private MockMvc mockMvc;

    private ShortUrlResponse sampleResponse;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ShortUrlController(service))
                .build();

        sampleResponse = new ShortUrlResponse(
                1L,
                "https://www.naver.com",
                "aB3xK912",
                "http://localhost:8080/r/aB3xK912",
                "네이버",
                UrlMode.NAMING,
                LocalDateTime.of(2026, 11, 16, 12, 0)
        );
    }

    @Test
    @DisplayName("단축 URL 생성 요청이 성공하면 HTTP 201을 반환한다")
    void createShortUrlTest() throws Exception {

        when(service.create(any())).thenReturn(sampleResponse);

        String requestBody = """
                {
                    "originalUrl": "www.naver.com",
                    "displayName": "네이버",
                    "mode": "NAMING"
                }
                """;

        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").value("aB3xK912"))
                .andExpect(jsonPath("$.displayName").value("네이버"));

        verify(service).create(any());
    }

    @Test
    @DisplayName("전체 단축 URL 목록을 조회하면 HTTP 200을 반환한다")
    void findAllTest() throws Exception {

        when(service.findAll())
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/urls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].displayName").value("네이버"));

        verify(service).findAll();
    }

    @Test
    @DisplayName("ID로 단축 URL을 조회하면 HTTP 200을 반환한다")
    void findByIdTest() throws Exception {

        when(service.findById(1L))
                .thenReturn(sampleResponse);

        mockMvc.perform(get("/api/urls/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("aB3xK912"));

        verify(service).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 ID를 조회하면 HTTP 404를 반환한다")
    void findByIdNotFoundTest() throws Exception {

        when(service.findById(999L))
                .thenThrow(new ShortUrlNotFoundException());

        mockMvc.perform(get("/api/urls/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("단축 URL로 접속하면 HTTP 302와 원본 URL을 반환한다")
    void redirectTest() throws Exception {

        when(service.findOriginalUrl("aB3xK912"))
                .thenReturn("https://www.naver.com");

        mockMvc.perform(get("/r/aB3xK912"))
                .andExpect(status().isFound())
                .andExpect(header().string(
                        "Location",
                        "https://www.naver.com"
                ));

        verify(service).findOriginalUrl("aB3xK912");
    }

    @Test
    @DisplayName("존재하지 않는 단축 코드로 접속하면 HTTP 404를 반환한다")
    void redirectNotFoundTest() throws Exception {

        when(service.findOriginalUrl("NOTFOUND"))
                .thenThrow(new ShortUrlNotFoundException());

        mockMvc.perform(get("/r/NOTFOUND"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("표시 이름 수정 요청이 성공하면 HTTP 200을 반환한다")
    void updateNameTest() throws Exception {

        ShortUrlResponse updated = new ShortUrlResponse(
                1L,
                sampleResponse.originalUrl(),
                sampleResponse.shortCode(),
                sampleResponse.shortUrl(),
                "네이버 메인",
                UrlMode.NAMING,
                sampleResponse.createdAt()
        );

        when(service.updateName(eq(1L), any()))
                .thenReturn(updated);

        String requestBody = """
                {
                    "displayName": "네이버 메인"
                }
                """;

        mockMvc.perform(put("/api/urls/1/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("네이버 메인"));

        verify(service).updateName(eq(1L), any());
    }

    @Test
    @DisplayName("단축 URL 삭제가 성공하면 HTTP 204를 반환한다")
    void deleteShortUrlTest() throws Exception {

        mockMvc.perform(delete("/api/urls/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    @DisplayName("존재하지 않는 단축 URL을 삭제하면 HTTP 404를 반환한다")
    void deleteShortUrlNotFoundTest() throws Exception {

        doThrow(new ShortUrlNotFoundException())
                .when(service).delete(999L);

        mockMvc.perform(delete("/api/urls/999"))
                .andExpect(status().isNotFound());

        verify(service).delete(999L);
    }
}
