package com.example.study.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.study.ApiTestSupport;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class StaticPageTest extends ApiTestSupport {

    @Test
    @DisplayName("첫 화면은 로그인 없이 열린다")
    void welcomePage() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(forwardedUrl("index.html"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "/index.html", "/css/style.css", "/js/app.js" })
    @DisplayName("화면 파일은 로그인 없이 받을 수 있다")
    void staticFiles(String path) throws Exception {
        mockMvc.perform(get(path)).andExpect(status().isOk());
    }
}
