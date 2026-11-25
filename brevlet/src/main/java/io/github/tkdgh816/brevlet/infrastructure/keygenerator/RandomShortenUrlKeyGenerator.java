package io.github.tkdgh816.brevlet.infrastructure.keygenerator;

import io.github.tkdgh816.brevlet.domain.key.ShortenUrlKeyGenerator;
import io.github.tkdgh816.brevlet.domain.key.ShortenUrlKeyRule;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class RandomShortenUrlKeyGenerator implements ShortenUrlKeyGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String generate() {
        StringBuilder sb = new StringBuilder(ShortenUrlKeyRule.KEY_LENGTH);
        for (int i = 0; i < ShortenUrlKeyRule.KEY_LENGTH; i++) {
            // BASE_56_CHARS애서 랜덤 위치 뽑아서 추가
            int index = RANDOM.nextInt(ShortenUrlKeyRule.BASE_56_CHARS.length());
            sb.append(ShortenUrlKeyRule.BASE_56_CHARS.charAt(index));
        }
        return sb.toString();
    }
}
