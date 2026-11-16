package com.example.personal_project.presentation.controller;

import com.example.personal_project.application.SimpleShortenUrlService;
import com.example.personal_project.global.exception.handler.GlobalExceptionHandler;
import com.example.personal_project.infrastructure.repository.MapShortenUrlRepository;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.LinkedHashMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ShortenUrlRestControllerTest {

    private static final String ORIGINAL_URL = "https://example.com/slides";
    private static final String CODE = "week03-jpa";

    private AnnotationConfigApplicationContext context;
    private MapShortenUrlRepository repository;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext();
        context.getEnvironment().setActiveProfiles("textbook");
        context.register(
                MapShortenUrlRepository.class,
                SimpleShortenUrlService.class,
                ShortenUrlRestController.class);
        context.refresh();
        repository = context.getBean(MapShortenUrlRepository.class);
        mvc = MockMvcBuilders.standaloneSetup(context.getBean(ShortenUrlRestController.class))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void customCodeRegistrationRedirectAndInformationUseSameLink() throws Exception {
        mvc.perform(post("/shortenUrl").contentType(MediaType.APPLICATION_JSON)
                        .content(body(ORIGINAL_URL, CODE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortenUrlKey").value(CODE))
                .andExpect(jsonPath("$.originalUrl").value(ORIGINAL_URL));

        mvc.perform(get("/study/{code}", CODE))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", ORIGINAL_URL))
                .andExpect(header().string("Cache-Control", "no-store"));

        mvc.perform(get("/shortenUrl/{code}", CODE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redirectCount").value(1));
        assertEquals(1L, repository.findShortenUrlByShortenUrlKey(CODE)
                .getRedirectCount().longValue());
    }

    @Test
    void customCodeWhitespaceIsRemovedBeforeResponseAndLookup() throws Exception {
        mvc.perform(post("/shortenUrl").contentType(MediaType.APPLICATION_JSON)
                        .content(body(ORIGINAL_URL, "  " + CODE + "  ")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortenUrlKey").value(CODE));

        mvc.perform(get("/study/{code}", CODE))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", ORIGINAL_URL));
        assertNull(repository.findShortenUrlByShortenUrlKey("  " + CODE + "  "));
    }

    @Test
    void duplicateCustomCodeReturns409AndPreservesExistingLink() throws Exception {
        mvc.perform(post("/shortenUrl").contentType(MediaType.APPLICATION_JSON)
                        .content(body(ORIGINAL_URL, CODE)))
                .andExpect(status().isOk());
        mvc.perform(get("/study/{code}", CODE)).andExpect(status().isFound());

        mvc.perform(post("/shortenUrl").contentType(MediaType.APPLICATION_JSON)
                        .content(body("https://example.com/replacement", CODE)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_CODE"))
                .andExpect(jsonPath("$.message").isString());

        assertEquals(ORIGINAL_URL,
                repository.findShortenUrlByShortenUrlKey(CODE).getOriginalUrl());
        assertEquals(1L, repository.findShortenUrlByShortenUrlKey(CODE)
                .getRedirectCount().longValue());
    }

    @Test
    void surroundingWhitespaceCannotBypassCodeUniqueness() throws Exception {
        mvc.perform(post("/shortenUrl").contentType(MediaType.APPLICATION_JSON)
                        .content(body(ORIGINAL_URL, CODE)))
                .andExpect(status().isOk());

        mvc.perform(post("/shortenUrl").contentType(MediaType.APPLICATION_JSON)
                        .content(body("https://example.com/replacement", "  " + CODE + "  ")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_CODE"));

        assertNull(repository.findShortenUrlByShortenUrlKey("  " + CODE + "  "));
        assertEquals(ORIGINAL_URL,
                repository.findShortenUrlByShortenUrlKey(CODE).getOriginalUrl());
    }

    @ParameterizedTest
    @MethodSource("invalidCustomCodes")
    void invalidCustomCodeReturnsUniform400(String code) throws Exception {
        mvc.perform(post("/shortenUrl").contentType(MediaType.APPLICATION_JSON)
                        .content(body(ORIGINAL_URL, code)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").isString());

        assertNull(repository.findShortenUrlByShortenUrlKey(code));
        assertNull(repository.findShortenUrlByShortenUrlKey(code.trim()));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"originalUrl\":\"https://example.com/slides\"}",
            "{\"originalUrl\":\"https://example.com/slides\",\"code\":null}"
    })
    void omittedOrNullCodeStillGeneratesEightCharacterLink(String requestBody) throws Exception {
        var result = mvc.perform(post("/shortenUrl").contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl").value(ORIGINAL_URL))
                .andExpect(jsonPath("$.shortenUrlKey").isString())
                .andReturn();

        String key = JsonPath.read(result.getResponse().getContentAsString(), "$.shortenUrlKey");
        assertTrue(key.matches("[a-zA-Z0-9]{8}"));
        assertNotNull(repository.findShortenUrlByShortenUrlKey(key));
        mvc.perform(get("/study/{code}", key))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", ORIGINAL_URL));
    }

    @ParameterizedTest
    @MethodSource("validOriginalUrlRequests")
    void validUrlIsStoredAndRedirectedWithoutModification(String originalUrl, String code)
            throws Exception {
        var result = mvc.perform(post("/shortenUrl").contentType(MediaType.APPLICATION_JSON)
                        .content(body(originalUrl, code)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl").value(originalUrl))
                .andReturn();

        String key = JsonPath.read(result.getResponse().getContentAsString(), "$.shortenUrlKey");
        if (code != null) {
            assertEquals(code, key);
        }
        assertEquals(originalUrl, repository.findShortenUrlByShortenUrlKey(key).getOriginalUrl());
        mvc.perform(get("/study/{code}", key))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", originalUrl))
                .andExpect(header().string("Cache-Control", "no-store"));
    }

    @ParameterizedTest
    @MethodSource("invalidOriginalUrlRequests")
    void invalidOriginalUrlReturnsUniform400WithoutChangingExistingLink(String originalUrl, String code)
            throws Exception {
        mvc.perform(post("/shortenUrl").contentType(MediaType.APPLICATION_JSON)
                        .content(body(ORIGINAL_URL, CODE)))
                .andExpect(status().isOk());
        mvc.perform(get("/study/{code}", CODE)).andExpect(status().isFound());

        mvc.perform(post("/shortenUrl").contentType(MediaType.APPLICATION_JSON)
                        .content(body(originalUrl, code)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").isString());

        assertNull(repository.findShortenUrlByShortenUrlKey("url-check"));
        assertEquals(ORIGINAL_URL, repository.findShortenUrlByShortenUrlKey(CODE).getOriginalUrl());
        assertEquals(1L, repository.findShortenUrlByShortenUrlKey(CODE)
                .getRedirectCount().longValue());
    }

    @ParameterizedTest
    @MethodSource("invalidDtoBodies")
    void invalidDtoIsRejectedBeforeServiceInvocation(String requestBody) throws Exception {
        SimpleShortenUrlService guardedService = mock(SimpleShortenUrlService.class);
        MockMvc validationMvc = MockMvcBuilders.standaloneSetup(new ShortenUrlRestController(guardedService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        validationMvc.perform(post("/shortenUrl").contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").isString());

        verifyNoInteractions(guardedService);
    }

    private static Stream<Arguments> validOriginalUrlRequests() {
        String prefix = "https://example.com/";
        return Stream.of(
                        "https://example.technology/slides",
                        "http://example.com/slides?week=3#summary",
                        "HTTPS://example.com/slides",
                        "https://example.com:8443/slides",
                        "https://example.com:65535/slides",
                        "https://example.com/a%20b?q=spring%20boot#part-1",
                        prefix + "a".repeat(2048 - prefix.length()))
                .flatMap(url -> Stream.of(Arguments.of(url, null), Arguments.of(url, "url-check")));
    }

    private static Stream<Arguments> invalidOriginalUrlRequests() {
        String prefix = "https://example.com/";
        return Stream.of(
                        null, "", "   ", prefix + "a".repeat(2049 - prefix.length()),
                        "ftp://example.com/slides", "javascript:alert(1)", "/slides/week03",
                        "https:///slides", "https://user:password@example.com/slides",
                        "https://@example.com/slides", "https://example.com:65536/slides",
                        "https://example.com:70000/slides", "https://example.com/has space",
                        "https://example.com/%ZZ", "http://", "//example.com/slides")
                .flatMap(url -> Stream.of(Arguments.of(url, null), Arguments.of(url, "url-check")));
    }

    private static Stream<String> invalidDtoBodies() {
        String prefix = "https://example.com/";
        return Stream.of(
                "{\"code\":\"url-check\"}",
                body(null, "url-check"),
                body("", "url-check"),
                body("   ", "url-check"),
                body(prefix + "a".repeat(2049 - prefix.length()), "url-check"));
    }

    private static Stream<String> invalidCustomCodes() {
        return Stream.of("", "   ", "ab", "a".repeat(41), "Week03", "-week03",
                "week03-", "week--03", "week/03", "week_03", "회차03", "week 03");
    }

    private static String body(String originalUrl, String code) {
        var request = new LinkedHashMap<String, Object>();
        request.put("originalUrl", originalUrl);
        request.put("code", code);
        return Configuration.defaultConfiguration().jsonProvider().toJson(request);
    }
}
