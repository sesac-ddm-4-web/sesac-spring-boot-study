package com.work.shortenurl.keygen;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class RandomBase62KeyGeneratorTest {

    private final KeyGenerator keyGenerator = new RandomBase62KeyGenerator();

    @Test
    void 키는_8자리다() {
        String key = keyGenerator.generate();

        assertThat(key).hasSize(8);
    }

    @Test
    void 키는_숫자와_영문_대소문자로만_구성된다() {
        for (int i = 0; i < 1_000; i++) {
            assertThat(keyGenerator.generate()).matches("^[0-9a-zA-Z]{8}$");
        }
    }

    @Test
    void 여러번_생성해도_중복되지_않는다() {
        Set<String> keys = new HashSet<>();

        for (int i = 0; i < 10_000; i++) {
            keys.add(keyGenerator.generate());
        }

        assertThat(keys).hasSize(10_000);
    }

    @Test
    void base62의_모든_문자가_사용된다() {
        Set<Character> usedChars = new HashSet<>();

        for (int i = 0; i < 10_000; i++) {
            for (char c : keyGenerator.generate().toCharArray()) {
                usedChars.add(c);
            }
        }

        assertThat(usedChars).hasSize(62);
    }
}
