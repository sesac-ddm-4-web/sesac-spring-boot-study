package com.example.personal_project.global.exception.handler;

import com.example.personal_project.global.exception.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {
    @RestController
    static class ErrorController {
        @GetMapping("/error/{type}")
        void fail(@PathVariable("type") String type) {
            switch(type) {
                case "input" -> throw new IllegalArgumentException("입력 오류");
                case "coupon" -> throw new InvalidCouponException("쿠폰 오류");
                case "row" -> throw new CartItemNotFoundException(1L);
                case "product" -> throw new ExternalProductNotFoundException(1L);
                case "local" -> throw new java.util.NoSuchElementException("상품 없음");
                case "duplicate" -> throw new DuplicationExternalProductException();
                case "conflict" -> throw new CartConflictException("재고 부족");
                default -> throw new ExternalApiException("secret upstream details");
            }
        }
        @GetMapping("/query") void query(@RequestParam("limit") int limit) {}
        @PostMapping("/body") void body(@RequestBody java.util.Map<String, Integer> body) {}
    }
    @Test void businessErrorsUseUniformJson() throws Exception {
        var mvc=MockMvcBuilders.standaloneSetup(new ErrorController()).setControllerAdvice(new GlobalExceptionHandler()).build();
        String[][] cases={{"input","400","INVALID_REQUEST"},{"coupon","400","INVALID_COUPON"},
            {"row","404","CART_ITEM_NOT_FOUND"},{"product","404","EXTERNAL_PRODUCT_NOT_FOUND"},
            {"local","404","ITEM_NOT_FOUND"},{"duplicate","409","DUPLICATE_EXTERNAL_PRODUCT"},
            {"conflict","409","CART_CONFLICT"},{"upstream","502","EXTERNAL_API_FAILURE"}};
        for(var c:cases) mvc.perform(get("/error/"+c[0])).andExpect(status().is(Integer.parseInt(c[1])))
            .andExpect(jsonPath("$.code").value(c[2])).andExpect(jsonPath("$.message").isString());
        mvc.perform(get("/error/upstream")).andExpect(jsonPath("$.message").value("외부 상품 서비스의 응답을 처리할 수 없습니다."));
    }
    @Test void bindingErrorsUseUniform400() throws Exception {
        var mvc=MockMvcBuilders.standaloneSetup(new ErrorController()).setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(get("/query")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        mvc.perform(get("/query").param("limit","abc")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        mvc.perform(post("/body").contentType("application/json").content("{"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }
}
