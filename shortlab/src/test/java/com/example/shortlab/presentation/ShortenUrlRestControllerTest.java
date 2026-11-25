package com.example.shortlab.presentation;

import com.example.shortlab.application.QrCodeService;
import com.example.shortlab.application.SimpleShortenUrlService;
import com.example.shortlab.domain.exception.NotFoundShortenUrlException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShortenUrlRestController.class)
class ShortenUrlRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SimpleShortenUrlService simpleShortenUrlService;

    @MockitoBean
    private QrCodeService qrCodeService;

    @Test
    @DisplayName("단축 URL 키로 요청하면 원래 URL로 리다이렉트되어야 한다.")
    void redirectShortenUrlTest() throws Exception {
        // given
        String shortenUrlKey = "aB3dE7xK";
        String originalUrl = "https://www.google.com";
        given(simpleShortenUrlService.getOriginalUrlByShortenUrlKey(shortenUrlKey))
                .willReturn(originalUrl);

        // when & then
        mockMvc.perform(get("/{shortenUrlKey}", shortenUrlKey))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, originalUrl));

        verify(simpleShortenUrlService).getOriginalUrlByShortenUrlKey(shortenUrlKey);
    }

    @Test
    @DisplayName("존재하지 않는 단축 URL 키로 요청하면 404가 반환되어야 한다.")
    void redirectNotExistShortenUrlTest() throws Exception {
        // given
        String notExistKey = "zzzzzzzz";
        given(simpleShortenUrlService.getOriginalUrlByShortenUrlKey(notExistKey))
                .willThrow(new NotFoundShortenUrlException());

        // when & then
        mockMvc.perform(get("/{shortenUrlKey}", notExistKey))
                .andExpect(status().isNotFound());
    }
}