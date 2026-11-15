package com.example.shorturl.link.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomShortUrlKeyGeneratorTest {

    private final RandomShortUrlKeyGenerator generator = new RandomShortUrlKeyGenerator();

    @Test
    @DisplayName("키는 8자이고 헷갈리는 문자(0, O, 1, I, l, o)를 쓰지 않는다.")
    void keyIsEightReadableCharacters() {
        for (int i = 0; i < 1000; i++) {
            String key = generator.generate();
            assertEquals(8, key.length());
            assertTrue(key.matches("[" + RandomShortUrlKeyGenerator.CHARACTERS + "]{8}"), key);
        }
    }

    @Test
    @DisplayName("1만 개를 만들어도 겹치는 키가 거의 없다.")
    void keysAreRandom() {
        Set<String> keys = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            keys.add(generator.generate());
        }
        assertTrue(keys.size() >= 9999);
    }
}
