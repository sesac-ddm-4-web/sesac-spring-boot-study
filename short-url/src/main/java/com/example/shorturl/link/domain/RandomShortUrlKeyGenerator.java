package com.example.shorturl.link.domain;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class RandomShortUrlKeyGenerator implements ShortUrlKeyGenerator {

    public static final int KEY_LENGTH = 8;
    // 헷갈리는 0 O 1 I l o 제외
    public static final String CHARACTERS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz";

    private final SecureRandom random = new SecureRandom();

    @Override
    public String generate() {
        StringBuilder key = new StringBuilder(KEY_LENGTH);
        for (int i = 0; i < KEY_LENGTH; i++) {
            key.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return key.toString();
    }
}
