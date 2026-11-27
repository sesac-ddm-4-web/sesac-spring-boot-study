package com.example.study.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import com.example.study.DatabaseCleaner;
import com.example.study.IntegrationTest;
import com.example.study.domain.Member;
import com.example.study.repository.MemberRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import tools.jackson.databind.ObjectMapper;

@IntegrationTest
class MemberApiTest {

    private static final String EMAIL = "kim@example.com";
    private static final String PASSWORD = "password123";

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    DatabaseCleaner databaseCleaner;

    @BeforeEach
    void clean() {
        databaseCleaner.clean();
    }

    @Test
    @DisplayName("가입하면 201과 회원 정보를 반환하고, 비밀번호는 응답에 없으며 BCrypt로 저장한다")
    void signUp() throws Exception {
        signUp(EMAIL, PASSWORD, "김회원", "안녕하세요")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.name").value("김회원"))
                .andExpect(jsonPath("$.introduction").value("안녕하세요"))
                .andExpect(jsonPath("$.password").doesNotExist());

        Member saved = memberRepository.findByEmail(EMAIL).orElseThrow();
        assertThat(saved.getPassword()).isNotEqualTo(PASSWORD).startsWith("$2");
        assertThat(passwordEncoder.matches(PASSWORD, saved.getPassword())).isTrue();
    }

    @Test
    @DisplayName("자기소개를 보내지 않으면 빈 문자열이다")
    void signUpWithoutIntroduction() throws Exception {
        send(post("/api/members"), Map.of("email", EMAIL, "password", PASSWORD, "name", "김회원"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.introduction").value(""));
    }

    @Test
    @DisplayName("가입만 해서는 로그인되지 않는다")
    void signUpDoesNotLogin() throws Exception {
        var result = signUp(EMAIL, PASSWORD, "김회원", "").andReturn();

        assertThat(result.getRequest().getSession(false)).isNull();
    }

    @Test
    @DisplayName("대소문자와 앞뒤 공백만 다른 이메일은 중복이며 409를 반환한다")
    void duplicateEmail() throws Exception {
        signUp(EMAIL, PASSWORD, "김회원", "").andExpect(status().isCreated());

        signUp("  KIM@Example.com ", PASSWORD, "다른이름", "")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));
        assertThat(memberRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("악센트가 다른 이메일은 서로 다른 이메일이다")
    void accentSensitiveEmail() throws Exception {
        signUp("jose@example.com", PASSWORD, "A", "").andExpect(status().isCreated());
        signUp("josé@example.com", PASSWORD, "B", "").andExpect(status().isCreated());
    }

    @ParameterizedTest(name = "{0}={1}")
    @CsvSource(delimiter = '|', textBlock = """
            email    | ''
            email    | not-an-email
            password | short12
            password | '        '
            name     | '   '
            name     | 1234567890123456789012345678901
            """)
    @DisplayName("가입 입력값이 규칙에 맞지 않으면 400과 해당 필드를 반환한다")
    void invalidSignUp(String field, String value) throws Exception {
        var body = new java.util.HashMap<>(Map.of("email", EMAIL, "password", PASSWORD, "name", "김회원"));
        body.put(field, value);

        send(post("/api/members"), body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value(field));
    }

    @Test
    @DisplayName("길이 상한: 비밀번호 64자, UTF-8 72바이트, 자기소개 500자")
    void lengthLimits() throws Exception {
        signUp("a@example.com", "a".repeat(64), "이름", "소".repeat(500)).andExpect(status().isCreated());
        signUp("b@example.com", "가".repeat(24), "이름", "").andExpect(status().isCreated());

        signUp("c@example.com", "a".repeat(65), "이름", "").andExpect(status().isBadRequest());
        signUp("d@example.com", "가".repeat(25), "이름", "").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("password"));
        signUp("e@example.com", PASSWORD, "이름", "소".repeat(501)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("introduction"));
    }

    @Test
    @DisplayName("로그인에 성공하면 세션에 회원 ID를 저장하고 비밀번호 없이 회원 정보를 반환한다")
    void login() throws Exception {
        signUp(EMAIL, PASSWORD, "김회원", "");

        var result = login(" KIM@example.com", PASSWORD, new MockHttpSession())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();

        Long memberId = memberRepository.findByEmail(EMAIL).orElseThrow().getId();
        assertThat(result.getRequest().getSession(false).getAttribute("memberId")).isEqualTo(memberId);
    }

    @Test
    @DisplayName("로그인에 성공하면 기존 세션을 버리고 새 세션 ID를 발급한다")
    void loginIssuesNewSession() throws Exception {
        signUp(EMAIL, PASSWORD, "김회원", "");
        MockHttpSession before = new MockHttpSession();

        var result = login(EMAIL, PASSWORD, before).andExpect(status().isOk()).andReturn();

        assertThat(before.isInvalid()).isTrue();
        assertThat(result.getRequest().getSession(false).getId()).isNotEqualTo(before.getId());
    }

    @ParameterizedTest
    @CsvSource({
            "kim@example.com,  wrong-password",
            "none@example.com, password123",
    })
    @DisplayName("없는 이메일과 틀린 비밀번호는 같은 401 응답이며 기존 세션은 유지한다")
    void loginFailure(String email, String password) throws Exception {
        signUp(EMAIL, PASSWORD, "김회원", "");
        MockHttpSession session = new MockHttpSession();

        login(email, password, session)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));

        assertThat(session.isInvalid()).isFalse();
        assertThat(session.getAttribute("memberId")).isNull();
    }

    @Test
    @DisplayName("72바이트를 넘는 비밀번호로 로그인하면 401이다")
    void loginWithTooLongPassword() throws Exception {
        signUp(EMAIL, "a".repeat(64), "김회원", "");

        login(EMAIL, "a".repeat(73), new MockHttpSession()).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그아웃하면 204이고 세션이 무효화된다")
    void logout() throws Exception {
        MockHttpSession session = signUpAndLogin();

        mockMvc.perform(post("/api/auth/logout").session(session)).andExpect(status().isNoContent());

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    @DisplayName("로그인하지 않으면 로그아웃·조회·수정은 401이다")
    void requiresLogin() throws Exception {
        for (MockHttpServletRequestBuilder request : new MockHttpServletRequestBuilder[] {
                post("/api/auth/logout"),
                get("/api/members/me"),
                // 입력값이 잘못돼도 로그인 확인이 먼저다.
                patch("/api/members/me").contentType(MediaType.APPLICATION_JSON).content("{") }) {
            mockMvc.perform(request)
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
        }
    }

    @Test
    @DisplayName("내 정보는 비밀번호 없이 반환한다")
    void getMe() throws Exception {
        MockHttpSession session = signUpAndLogin();

        mockMvc.perform(get("/api/members/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.name").value("김회원"))
                .andExpect(jsonPath("$.introduction").value("소개"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("보낸 필드만 수정하고, 자기소개는 빈 문자열로 지울 수 있다")
    void partialUpdate() throws Exception {
        MockHttpSession session = signUpAndLogin();

        update(session, "{\"name\":\"새이름\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("새이름"))
                .andExpect(jsonPath("$.introduction").value("소개"));
        update(session, "{\"introduction\":\"\",\"name\":null}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("새이름"))
                .andExpect(jsonPath("$.introduction").value(""));

        mockMvc.perform(get("/api/members/me").session(session))
                .andExpect(jsonPath("$.name").value("새이름"))
                .andExpect(jsonPath("$.introduction").value(""));
    }

    @Test
    @DisplayName("이메일 등 다른 필드는 무시한다")
    void ignoresOtherFields() throws Exception {
        MockHttpSession session = signUpAndLogin();

        update(session, "{\"name\":\"새이름\",\"email\":\"hacked@example.com\",\"password\":\"changed123\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL));

        login(EMAIL, PASSWORD, new MockHttpSession()).andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            {}
            {"name":null,"introduction":null}
            {"email":"other@example.com"}
            {"name":""}
            {"name":"   "}
            {"name":"1234567890123456789012345678901"}
            """)
    @DisplayName("수정할 값이 없거나 이름이 잘못되면 400이다")
    void invalidUpdate(String json) throws Exception {
        MockHttpSession session = signUpAndLogin();

        update(session, json).andExpect(status().isBadRequest());
    }

    private ResultActions signUp(String email, String password, String name, String introduction) throws Exception {
        return send(post("/api/members"),
                Map.of("email", email, "password", password, "name", name, "introduction", introduction));
    }

    private ResultActions login(String email, String password, MockHttpSession session) throws Exception {
        return send(post("/api/auth/login").session(session), Map.of("email", email, "password", password));
    }

    private MockHttpSession signUpAndLogin() throws Exception {
        signUp(EMAIL, PASSWORD, "김회원", "소개").andExpect(status().isCreated());
        var result = login(EMAIL, PASSWORD, new MockHttpSession()).andExpect(status().isOk()).andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private ResultActions update(MockHttpSession session, String json) throws Exception {
        return mockMvc.perform(patch("/api/members/me").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private ResultActions send(MockHttpServletRequestBuilder request, Object body) throws Exception {
        return mockMvc.perform(request
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }
}
