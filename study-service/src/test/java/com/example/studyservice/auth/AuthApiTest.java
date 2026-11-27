package com.example.studyservice.auth;

import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
class AuthApiTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private String email;

    @BeforeEach
    void setUp() {
        Filter securityFilter = context.getBean("springSecurityFilterChain", Filter.class);
        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(securityFilter).build();
        email = "user-" + UUID.randomUUID() + "@test.com";
    }

    private MvcResult postJson(String url, String body, String accessToken) throws Exception {
        var request = post(url).contentType(MediaType.APPLICATION_JSON).content(body);
        if (accessToken != null) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        }
        return mockMvc.perform(request).andReturn();
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private JsonNode signupAndLogin() throws Exception {
        MvcResult signup = postJson("/auth/signup",
                "{\"email\":\"" + email + "\",\"password\":\"password1234\",\"name\":\"홍길동\"}", null);
        assertEquals(201, signup.getResponse().getStatus());

        MvcResult login = postJson("/auth/login",
                "{\"email\":\"" + email + "\",\"password\":\"password1234\"}", null);
        assertEquals(200, login.getResponse().getStatus());
        return json(login);
    }

    @Test
    @DisplayName("로그인으로 받은 액세스 토큰으로 내 정보를 조회한다.")
    void loginAndAccess() throws Exception {
        String accessToken = signupAndLogin().get("accessToken").asString();

        MvcResult me = mockMvc.perform(get("/members/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)).andReturn();

        assertEquals(200, me.getResponse().getStatus());
        assertEquals(email, json(me).get("email").asString());
        assertEquals("홍길동", json(me).get("profile").get("nickname").asString());
    }

    @Test
    @DisplayName("토큰이 없거나 잘못되면 401과 사유 코드를 돌려준다.")
    void unauthorized() throws Exception {
        MvcResult noToken = mockMvc.perform(get("/members/me")).andReturn();
        assertEquals(401, noToken.getResponse().getStatus());
        assertEquals("UNAUTHORIZED", json(noToken).get("code").asString());

        MvcResult badToken = mockMvc.perform(get("/members/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt")).andReturn();
        assertEquals(401, badToken.getResponse().getStatus());
        assertEquals("INVALID_TOKEN", json(badToken).get("code").asString());
    }

    @Test
    @DisplayName("스터디 목록은 로그인 없이 보이지만 참여자 목록과 참여는 로그인해야 한다.")
    void publicAndProtectedStudyApi() throws Exception {
        assertEquals(200, mockMvc.perform(get("/studies")).andReturn().getResponse().getStatus());
        assertEquals(401, mockMvc.perform(get("/studies/1/members")).andReturn().getResponse().getStatus());
        assertEquals(401, postJson("/studies/1/members", "{}", null).getResponse().getStatus());
        assertEquals(401, mockMvc.perform(delete("/studies/1/members/me")).andReturn()
                .getResponse().getStatus());
    }

    @Test
    @DisplayName("리프레시 토큰은 재발급 때마다 바뀌고, 이전 토큰과 로그아웃한 토큰은 쓸 수 없다.")
    void reissueRotationAndLogout() throws Exception {
        JsonNode tokens = signupAndLogin();
        String firstRefresh = tokens.get("refreshToken").asString();

        MvcResult reissued = postJson("/auth/reissue", "{\"refreshToken\":\"" + firstRefresh + "\"}", null);
        assertEquals(200, reissued.getResponse().getStatus());
        String secondRefresh = json(reissued).get("refreshToken").asString();
        String secondAccess = json(reissued).get("accessToken").asString();
        assertNotEquals(firstRefresh, secondRefresh);

        MvcResult reuseOld = postJson("/auth/reissue", "{\"refreshToken\":\"" + firstRefresh + "\"}", null);
        assertEquals(401, reuseOld.getResponse().getStatus());
        assertEquals("INVALID_REFRESH_TOKEN", json(reuseOld).get("code").asString());

        assertEquals(204, postJson("/auth/logout", "", secondAccess).getResponse().getStatus());
        MvcResult afterLogout = postJson("/auth/reissue", "{\"refreshToken\":\"" + secondRefresh + "\"}", null);
        assertEquals(401, afterLogout.getResponse().getStatus());
    }

    @Test
    @DisplayName("같은 이메일로 다시 가입하면 409, 비밀번호가 틀리면 401")
    void duplicateEmailAndWrongPassword() throws Exception {
        signupAndLogin();

        MvcResult duplicate = postJson("/auth/signup",
                "{\"email\":\"" + email.toUpperCase() + "\",\"password\":\"password1234\",\"name\":\"김철수\"}", null);
        assertEquals(409, duplicate.getResponse().getStatus());
        assertEquals("DUPLICATE_EMAIL", json(duplicate).get("code").asString());

        MvcResult wrong = postJson("/auth/login",
                "{\"email\":\"" + email + "\",\"password\":\"wrong-password\"}", null);
        assertEquals(401, wrong.getResponse().getStatus());
        assertEquals("LOGIN_FAILED", json(wrong).get("code").asString());
    }
}
