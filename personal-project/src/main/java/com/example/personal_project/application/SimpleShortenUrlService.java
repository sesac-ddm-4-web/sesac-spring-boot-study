package com.example.personal_project.application;

import com.example.personal_project.domain.ShortenUrl;
import com.example.personal_project.domain.ShortenUrlRepository;
import com.example.personal_project.global.exception.DuplicateShortenUrlCodeException;
import com.example.personal_project.global.exception.LackOfShortenUrlKeyException;
import com.example.personal_project.global.exception.NotFoundShortenException;
import com.example.personal_project.presentation.dto.url.ShortenUrlCreateRequestDto;
import com.example.personal_project.presentation.dto.url.ShortenUrlCreateResponseDto;
import com.example.personal_project.presentation.dto.url.ShortenUrlInformationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

@Service
public class SimpleShortenUrlService {

    private ShortenUrlRepository shortenUrlRepository;

    final int MAX_ATTEMPTS = 5;
    private static final Pattern CUSTOM_CODE_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    @Autowired
    SimpleShortenUrlService(ShortenUrlRepository shortenUrlRepository) {
        this.shortenUrlRepository = shortenUrlRepository;
    }

    public ShortenUrlCreateResponseDto generateShortenUrl(
            ShortenUrlCreateRequestDto shortenUrlCreateRequestDto
    ) {
        String originalUrl = shortenUrlCreateRequestDto.getOriginalUrl();
        validateOriginalUrl(originalUrl);
        String requestedCode = shortenUrlCreateRequestDto.getCode();

        if (requestedCode != null) {
            return createWithCustomCode(originalUrl, requestedCode);
        }

        return createWithAutomaticCode(originalUrl);
    }

    private void validateOriginalUrl(String originalUrl) {
        if (originalUrl == null || originalUrl.isBlank()) {
            throw new IllegalArgumentException("원본 URL을 입력해주세요.");
        }

        if (originalUrl.length() > 2048) {
            throw new IllegalArgumentException("원본 URL은 2048자 이하여야 합니다.");
        }

        URI uri;
        try {
            uri = new URI(originalUrl);
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("올바른 URL 형식으로 입력해주세요.", exception);
        }

        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("http 또는 https 주소만 등록할 수 있습니다.");
        }

        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("호스트가 포함된 올바른 URL을 입력해주세요.");
        }

        if (uri.getRawUserInfo() != null) {
            throw new IllegalArgumentException("사용자 정보가 포함된 URL은 등록할 수 없습니다.");
        }

        if (uri.getPort() > 65535) {
            throw new IllegalArgumentException("올바른 포트 번호를 입력해주세요.");
        }
    }

    private ShortenUrlCreateResponseDto createWithAutomaticCode(String originalUrl) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String key = ShortenUrl.generateShortenUrlKey();
            ShortenUrl candidate = new ShortenUrl(originalUrl, key);

            boolean saved = shortenUrlRepository.saveShortenUrlIfAbsent(candidate);

            if (saved) {
                return new ShortenUrlCreateResponseDto(candidate);
            }
        }

        throw new LackOfShortenUrlKeyException("URL 생성에 실패했습니다.");
    }

    private ShortenUrlCreateResponseDto createWithCustomCode(String originalUrl, String requestedCode) {
        String code = requestedCode.trim();

        if (code.length() < 3
                || code.length() > 40
                || !CUSTOM_CODE_PATTERN.matcher(code).matches()) {
            throw new IllegalArgumentException(
                    "단축 코드는 3~40자의 영문 소문자·숫자·하이픈으로 입력해주세요. "
                            + "하이픈은 시작·끝 또는 연속해서 사용할 수 없습니다."
            );
        }

        ShortenUrl candidate = new ShortenUrl(originalUrl, code);
        boolean saved = shortenUrlRepository.saveShortenUrlIfAbsent(candidate);

        if (!saved) {
            throw new DuplicateShortenUrlCodeException("이미 사용 중인 단축 주소입니다.");
        }

        return new ShortenUrlCreateResponseDto(candidate);
    }

    private String getUniqueShortenUrlKEty() {
        final int MAX_RETRY_COUNT = 5;
        int count = 0;

        while (count++ < MAX_RETRY_COUNT) {
            String shortenUrlKey = ShortenUrl.generateShortenUrlKey();
            ShortenUrl shortenUrl = shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

            if (null == shortenUrl) return shortenUrlKey;
        }

        throw new LackOfShortenUrlKeyException("URL 생성에 실패했습니다.");
    }

    public ShortenUrlInformationDto getShortenUrlInformationByShortenUrlKey(String shortenUrlKey) {
        ShortenUrl shortenUrl = shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

        if (null == shortenUrl) throw new NotFoundShortenException("해당 URL이 등록되어있지 않습니다.");

        ShortenUrlInformationDto shortenUrlInformationDto = new ShortenUrlInformationDto(shortenUrl);

        return shortenUrlInformationDto;
    }

    public String getOriginalUrlByShortenUrlKey(String shortenUrlKey) {
        ShortenUrl shortenUrl = shortenUrlRepository.increaseRedirectCount(shortenUrlKey);

        if (null == shortenUrl) throw new NotFoundShortenException("해당 URL이 등록되어있지 않습니다.");

        String originalUrl = shortenUrl.getOriginalUrl();

        return originalUrl;
    }

}
