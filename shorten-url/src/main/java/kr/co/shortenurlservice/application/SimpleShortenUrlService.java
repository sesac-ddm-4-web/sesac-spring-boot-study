
package kr.co.shortenurlservice.application;

import kr.co.shortenurlservice.domain.ShortenUrl;
import kr.co.shortenurlservice.domain.ShortenUrlRepository;
import kr.co.shortenurlservice.presentation.*;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SimpleShortenUrlService {

    private final ShortenUrlRepository shortenUrlRepository;

    public SimpleShortenUrlService(
            ShortenUrlRepository shortenUrlRepository
    ) {
        this.shortenUrlRepository = shortenUrlRepository;
    }

    // 단축 URL 생성
    public ShortenUrlCreateResponseDto generateShortenUrl(
            ShortenUrlCreateRequestDto request
    ) {

        String originalUrl = request.getOriginalUrl();

        for (int attempt = 0; attempt < 5; attempt++) {

            String key = ShortenUrl.generateShortenUrlKey();
            ShortenUrl shortenUrl = new ShortenUrl(originalUrl, key);

            try {
                shortenUrlRepository.saveShortenUrl(shortenUrl);
                return new ShortenUrlCreateResponseDto(shortenUrl);

            } catch (DuplicateKeyException e) {
                // 단축 코드 충돌 시 재생성
            }
        }

        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "단축 URL 생성에 실패했습니다."
        );
    }

    // 단축 URL 상세 정보 조회
    public ShortenUrlInformationDto getShortenUrlInformationByShortenUrlKey(
            String key
    ) {

        ShortenUrl shortenUrl = findByKey(key);

        return new ShortenUrlInformationDto(
                shortenUrl.getOriginalUrl(),
                shortenUrl.getShortenUrlKey(),
                shortenUrl.getRedirectCount()
        );
    }

    // 단축 URL 접속 및 클릭 횟수 증가
    public String getOriginalUrlByShortenUrlKey(String key) {

        ShortenUrl shortenUrl = findByKey(key);

        if (!shortenUrl.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "중지된 단축 URL입니다."
            );
        }

        boolean updated =
                shortenUrlRepository.increaseRedirectCount(key);

        if (!updated) {
            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "사용할 수 없는 단축 URL입니다."
            );
        }

        return shortenUrl.getOriginalUrl();
    }

    // 전체 목록 조회 + 검색 + 페이지네이션
    public ShortenUrlListResponseDto getShortenUrls(
            String keyword,
            int page,
            int size
    ) {

        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "페이지 번호 또는 크기가 올바르지 않습니다."
            );
        }

        String searchKeyword =
                keyword == null ? "" : keyword.trim();

        long offsetValue = (long) page * size;

        if (offsetValue > Integer.MAX_VALUE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "페이지 범위를 초과했습니다."
            );
        }

        List<ShortenUrl> urls = shortenUrlRepository.findAll(
                searchKeyword,
                size,
                (int) offsetValue
        );

        long total = shortenUrlRepository.count(searchKeyword);

        List<ShortenUrlListResponseDto.ShortenUrlItemDto> items =
                urls.stream()
                        .map(url ->
                                new ShortenUrlListResponseDto.ShortenUrlItemDto(
                                        url.getOriginalUrl(),
                                        url.getShortenUrlKey(),
                                        url.getRedirectCount(),
                                        url.isActive()
                                )
                        )
                        .toList();

        int totalPages =
                (int) Math.ceil((double) total / size);

        return new ShortenUrlListResponseDto(
                items,
                page,
                size,
                total,
                totalPages
        );
    }

    // URL 사용 중지 및 재개
    public void updateShortenUrlStatus(
            String key,
            boolean active
    ) {

        findByKey(key);

        shortenUrlRepository.updateStatus(key, active);
    }

    // 원본 URL 변경 (신규)
    public void updateOriginalUrl(
            String key,
            String originalUrl
    ) {

        findByKey(key);

        boolean updated = shortenUrlRepository.updateOriginalUrl(
                key,
                originalUrl
        );

        if (!updated) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "원본 URL을 변경할 수 없습니다."
            );
        }
    }

    // 단축 URL 존재 여부 확인
    private ShortenUrl findByKey(String key) {

        ShortenUrl shortenUrl =
                shortenUrlRepository.findShortenUrlByShortenUrlKey(key);

        if (shortenUrl == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "존재하지 않는 단축 URL입니다."
            );
        }

        return shortenUrl;
    }
}
