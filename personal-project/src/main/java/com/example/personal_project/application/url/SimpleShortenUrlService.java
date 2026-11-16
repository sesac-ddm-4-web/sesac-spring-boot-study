package com.example.personal_project.application.url;

import com.example.personal_project.application.url.command.CreateShortenUrlCommand;
import com.example.personal_project.application.url.port.ShortCodeGenerator;
import com.example.personal_project.application.url.result.ShortenUrlResult;
import com.example.personal_project.domain.url.exception.DuplicateShortenUrlCodeException;
import com.example.personal_project.domain.url.exception.LackOfShortenUrlKeyException;
import com.example.personal_project.domain.url.exception.NotFoundShortenException;
import com.example.personal_project.domain.url.model.OriginalUrl;
import com.example.personal_project.domain.url.model.ShortCode;
import com.example.personal_project.domain.url.model.ShortenUrl;
import com.example.personal_project.domain.url.repository.ShortenUrlRepository;
import org.springframework.stereotype.Service;

@Service
public class SimpleShortenUrlService {

    private final ShortenUrlRepository shortenUrlRepository;

    private static final int MAX_ATTEMPTS = 5;
    private final ShortCodeGenerator generator;

    public SimpleShortenUrlService(ShortenUrlRepository shortenUrlRepository, ShortCodeGenerator generator) {
        this.shortenUrlRepository = shortenUrlRepository;
        this.generator = generator;
    }

    public ShortenUrlResult generateShortenUrl(
            CreateShortenUrlCommand command
    ) {
        String originalUrl = command.originalUrl();
        new OriginalUrl(originalUrl);
        String requestedCode = command.code();

        if (requestedCode != null) {
            return createWithCustomCode(originalUrl, requestedCode);
        }

        return createWithAutomaticCode(originalUrl);
    }

    private ShortenUrlResult createWithAutomaticCode(String originalUrl) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String key = generator.generate();
            ShortenUrl candidate = new ShortenUrl(originalUrl, key);

            boolean saved = shortenUrlRepository.saveShortenUrlIfAbsent(candidate);

            if (saved) {
                return ShortenUrlResult.from(candidate);
            }
        }

        throw new LackOfShortenUrlKeyException("URL 생성에 실패했습니다.");
    }

    private ShortenUrlResult createWithCustomCode(String originalUrl, String requestedCode) {
        String code = new ShortCode(requestedCode).value();

        ShortenUrl candidate = new ShortenUrl(originalUrl, code);
        boolean saved = shortenUrlRepository.saveShortenUrlIfAbsent(candidate);

        if (!saved) {
            throw new DuplicateShortenUrlCodeException("이미 사용 중인 단축 주소입니다.");
        }

        return ShortenUrlResult.from(candidate);
    }

    public ShortenUrlResult getShortenUrlInformationByShortenUrlKey(String shortenUrlKey) {
        ShortenUrl shortenUrl = shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

        if (null == shortenUrl) throw new NotFoundShortenException("해당 URL이 등록되어있지 않습니다.");

        ShortenUrlResult shortenUrlInformationDto = ShortenUrlResult.from(shortenUrl);

        return shortenUrlInformationDto;
    }

    public String getOriginalUrlByShortenUrlKey(String shortenUrlKey) {
        ShortenUrl shortenUrl = shortenUrlRepository.increaseRedirectCount(shortenUrlKey);

        if (null == shortenUrl) throw new NotFoundShortenException("해당 URL이 등록되어있지 않습니다.");

        String originalUrl = shortenUrl.getOriginalUrl();

        return originalUrl;
    }

}
