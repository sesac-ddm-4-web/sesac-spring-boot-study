package com.example.personal_project.infrastructure.url;

import com.example.personal_project.application.url.port.ShortCodeGenerator;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class RandomShortCodeGenerator implements ShortCodeGenerator {
    private static final String ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz";
    public String generate() {
        StringBuilder result = new StringBuilder(8);
        for (int i = 0; i < 8; i++) result.append(ALPHABET.charAt(ThreadLocalRandom.current().nextInt(ALPHABET.length())));
        return result.toString();
    }
}
