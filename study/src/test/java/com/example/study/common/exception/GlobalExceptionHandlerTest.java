package com.example.study.common.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.ErrorTestController.class)
@Import(GlobalExceptionHandlerTest.ErrorTestController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("업무 예외는 ErrorCode의 상태 코드와 문구를 반환한다")
    void businessException() throws Exception {
        expectError(mockMvc.perform(get("/test/business")), 404, "요청한 대상을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("본문 검증 실패는 400과 필드별 오류를 반환한다")
    void invalidBody() throws Exception {
        expectValidationError(mockMvc.perform(post("/test/body")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\" \"}")))
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("이름을 입력해 주세요."));
    }

    @Test
    @DisplayName("경로 변수 제약 위반은 400과 필드별 오류를 반환한다")
    void invalidPathVariableValue() throws Exception {
        expectValidationError(mockMvc.perform(get("/test/items/0")))
                .andExpect(jsonPath("$.errors[0].field").value("id"))
                .andExpect(jsonPath("$.errors[0].message").value("1 이상이어야 합니다."));
    }

    @Test
    @DisplayName("깨진 JSON은 400을 반환한다")
    void malformedJson() throws Exception {
        expectError(mockMvc.perform(post("/test/body")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{")), 400, "입력값이 올바르지 않습니다.");
    }

    @Test
    @DisplayName("경로 변수의 타입이 다르면 400을 반환한다")
    void typeMismatch() throws Exception {
        expectError(mockMvc.perform(get("/test/items/abc")), 400, "입력값이 올바르지 않습니다.");
    }

    @Test
    @DisplayName("없는 URL은 404를 반환한다")
    void unknownUrl() throws Exception {
        expectError(mockMvc.perform(get("/no-such-url")), 404, "요청한 대상을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("지원하지 않는 Method는 405와 Allow 헤더를 반환한다")
    void methodNotAllowed() throws Exception {
        expectError(mockMvc.perform(get("/test/body")), 405, "지원하지 않는 요청 방식입니다.")
                .andExpect(header().string(HttpHeaders.ALLOW, "POST"));
    }

    @Test
    @DisplayName("지원하지 않는 Content-Type은 415를 반환한다")
    void unsupportedMediaType() throws Exception {
        expectError(mockMvc.perform(post("/test/body")
                .contentType(MediaType.TEXT_PLAIN)
                .content("name")), 415, "지원하지 않는 Content-Type입니다.");
    }

    @Test
    @DisplayName("예상하지 못한 예외는 500을 반환하고 내부 정보를 숨긴다")
    void unexpectedException() throws Exception {
        expectError(mockMvc.perform(get("/test/unexpected")), 500, "서버에서 오류가 발생했습니다.");
    }

    private ResultActions expectError(ResultActions result, int code, String message) throws Exception {
        return result
                .andExpect(status().is(code))
                .andExpect(jsonPath("$.status").value(code))
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    private ResultActions expectValidationError(ResultActions result) throws Exception {
        return result
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."));
    }

    record TestRequest(@NotBlank(message = "이름을 입력해 주세요.") String name) {
    }

    @RestController
    static class ErrorTestController {

        @GetMapping("/test/business")
        void business() {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        @PostMapping("/test/body")
        void body(@Valid @RequestBody TestRequest request) {
        }

        @GetMapping("/test/items/{id}")
        void item(@PathVariable("id") @Min(value = 1, message = "1 이상이어야 합니다.") Long id) {
        }

        @GetMapping("/test/unexpected")
        void unexpected() {
            throw new IllegalStateException("노출되면 안 되는 내부 정보");
        }
    }
}
