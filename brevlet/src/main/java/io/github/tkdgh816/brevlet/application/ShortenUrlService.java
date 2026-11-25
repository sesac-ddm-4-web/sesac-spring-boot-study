package io.github.tkdgh816.brevlet.application;

import io.github.tkdgh816.brevlet.domain.key.KeyGenerationFailedException;
import io.github.tkdgh816.brevlet.domain.key.ShortenUrlKeyConflictException;
import io.github.tkdgh816.brevlet.domain.key.ShortenUrlKeyGenerator;
import io.github.tkdgh816.brevlet.domain.url.ShortenUrl;
import io.github.tkdgh816.brevlet.domain.url.ShortenUrlNotFoundException;
import io.github.tkdgh816.brevlet.domain.url.ShortenUrlRepository;
import io.github.tkdgh816.brevlet.domain.url.ShortenUrlUnavailableException;
import io.github.tkdgh816.brevlet.dto.url.CreateShortenUrlRequest;
import io.github.tkdgh816.brevlet.dto.url.ShortenUrlResponse;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class ShortenUrlService {

    private final ShortenUrlRepository shortenUrlRepository;
    private final ShortenUrlKeyGenerator shortenUrlKeyGenerator;
    private final Clock clock;

    public ShortenUrlService(ShortenUrlRepository shortenUrlRepository, ShortenUrlKeyGenerator shortenUrlKeyGenerator, Clock clock) {
        this.shortenUrlRepository = shortenUrlRepository;
        this.shortenUrlKeyGenerator = shortenUrlKeyGenerator;
        this.clock = clock;
    }

    private static final int MAX_GENERATE_COUNT = 5;

    public ShortenUrlResponse generateShortenUrl(CreateShortenUrlRequest request) {
        // 만료 시간 계산
        Integer expiresInDays = request.expiresInDays();
        Instant expiresAt = expiresInDays == null
                ? null
                : clock.instant().plus(Duration.ofDays(expiresInDays));

        // 단축 URL 키를 만들어 저장 시도
        // 저장 실패 시 재시도 횟수만큼 재시도
        for (int attempt = 0; attempt < MAX_GENERATE_COUNT; attempt++) {
            String shortenUrlKey = shortenUrlKeyGenerator.generate();
            try {
                // ShortenUrl 도메인 객체 생성 및 레포지토리 저장
                ShortenUrl shortenUrl = new ShortenUrl(request.originalUrl(), shortenUrlKey, expiresAt);
                shortenUrlRepository.save(shortenUrl);
                return ShortenUrlResponse.from(shortenUrl);
            } catch (ShortenUrlKeyConflictException e) {
                // 새 키 생성 다시 시도
            }
        }

        throw new KeyGenerationFailedException("단축 URL을 만들 수 없습니다.");
    }

    public ShortenUrlResponse getShortenUrlInfoByKey(String shortenUrlKey) {
        // 레포지토리에서 key에 해당하는 ShortenUrl 요청
        ShortenUrl shortenUrl = getByKey(shortenUrlKey);

        // 요청 응답
        return ShortenUrlResponse.from(shortenUrl);
    }

    public String getOriginalUrlByKey(String shortenUrlKey) {
        // 레포지토리에서 key에 해당하는 ShortenUrl 요청
        ShortenUrl shortenUrl = getByKey(shortenUrlKey);

        // 링크가 만료되었는지 확인
        if (!shortenUrl.isAvailable(clock.instant())) {
            throw new ShortenUrlUnavailableException("단축 URL을 사용할 수 없습니다.");
        }

        // 레포지토리에 리디렉션 횟수 증가 요청
        shortenUrlRepository.increaseRedirectCount(shortenUrlKey);

        // 원본 링크 반환
        return shortenUrl.getOriginalUrl();
    }

    private ShortenUrl getByKey(String shortenUrlKey) {
        return shortenUrlRepository.findByKey(shortenUrlKey)
                .orElseThrow(() -> new ShortenUrlNotFoundException("단축 URL(%s)를 찾을 수 없습니다.".formatted(shortenUrlKey)));
    }
}
