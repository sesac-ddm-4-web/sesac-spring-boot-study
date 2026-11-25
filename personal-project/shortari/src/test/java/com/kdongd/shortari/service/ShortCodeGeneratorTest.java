
package com.kdongd.shortari.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ShortCodeGeneratorTest {

    private final ShortCodeGenerator generator = new ShortCodeGenerator();

    @Test
    @DisplayName("생성된 단축 코드는 8자리여야 한다")
    void generateCodeLengthTest() {
        String code = generator.generate();

        assertEquals(8, code.length());
    }

    @Test
    @DisplayName("생성된 단축 코드는 영문 대소문자와 숫자로만 구성되어야 한다")
    void generateBase62CodeTest() {
        String code = generator.generate();

        assertTrue(code.matches("[0-9A-Za-z]{8}"));
    }

    @Test
    @DisplayName("단축 코드를 100번 생성하면 모두 유효한 형식이어야 한다")
    void generateMultipleCodesTest() {
        for (int i = 0; i < 100; i++) {
            String code = generator.generate();

            assertNotNull(code);
            assertTrue(code.matches("[0-9A-Za-z]{8}"));
        }
    }

    @Test
    @DisplayName("100개의 단축 코드를 생성했을 때 중복이 발생하지 않는지 확인한다")
    void generateUniqueCodesTest() {
        Set<String> codes = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            codes.add(generator.generate());
        }

        assertEquals(100, codes.size());
    }
}
