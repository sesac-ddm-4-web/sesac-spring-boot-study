package com.example.shortlab.application;

import com.example.shortlab.domain.ClickLog;
import com.example.shortlab.domain.ClickLogRepository;
import com.example.shortlab.domain.ShortenUrl;
import com.example.shortlab.domain.ShortenUrlRepository;
import com.example.shortlab.domain.exception.DuplicateShortenUrlKeyException;
import com.example.shortlab.domain.exception.InvalidOriginalUrlException;
import com.example.shortlab.domain.exception.LackOfShortenUrlKeyException;
import com.example.shortlab.domain.exception.NotFoundShortenUrlException;
import com.example.shortlab.infrastructure.clicklog.ClickLogEntity;
import com.example.shortlab.infrastructure.clicklog.ClickLogJpaRepository;
import com.example.shortlab.presentation.dto.ClickLogPageResponseDto;
import com.example.shortlab.presentation.dto.ShortenUrlCreateRequestDto;
import com.example.shortlab.presentation.dto.ShortenUrlCreateResponseDto;
import com.example.shortlab.presentation.dto.ShortenUrlInformationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

@Service
public class SimpleShortenUrlService {

    private ShortenUrlRepository shortenUrlRepository;
    private ClickLogRepository clickLogRepository;
    private static final Set<String> RESERVED = Set.of("shortenurl", "assets", "error", "index", "favicon");

    @Autowired
    SimpleShortenUrlService(ShortenUrlRepository shortenUrlRepository, ClickLogRepository clickLogRepository) {
        this.shortenUrlRepository = shortenUrlRepository;
        this.clickLogRepository = clickLogRepository;
    }

    @Transactional
    public ShortenUrlCreateResponseDto generateShortenUrl(
            ShortenUrlCreateRequestDto shortenUrlCreateRequestDto)
    {
        String key;

        if (shortenUrlCreateRequestDto.getAlias() != null) {
            key = checkedAlias(shortenUrlCreateRequestDto.getAlias());
        }

        else {
            String original = shortenUrlCreateRequestDto.getOriginalUrl();
            validateUri(original);
            key = getUniqueShortenUrlKey();
        }

        // 새로운 단축 url 만들고 저장
        ShortenUrl shortenUrl = new ShortenUrl(shortenUrlCreateRequestDto.getOriginalUrl(),key);
        try {
            shortenUrlRepository.saveShortenUrl(shortenUrl);
        } catch (DataIntegrityViolationException e) {
            // check-then-insert 사이에 동시 요청이 먼저 저장한 경우: unique 제약이 최종 방어선
            throw new DuplicateShortenUrlKeyException();
        }

        ShortenUrlCreateResponseDto shortenUrlCreateResponseDto = new ShortenUrlCreateResponseDto(shortenUrl);
        return shortenUrlCreateResponseDto;
    }

    // check 별칭
    private String checkedAlias(String alias) {
        if (RESERVED.contains(alias.toLowerCase()) || shortenUrlRepository.existsByShortenUrlKey(alias)) {
            throw new DuplicateShortenUrlKeyException();
        }
        return alias;
    }
    // 입력 경로 검사
    private void validateUri(String originalUrl){
        try {
            new URI(originalUrl);
        } catch (URISyntaxException e){
            throw new InvalidOriginalUrlException();
        }
    }
    // unique 단축 url 생성
    private String getUniqueShortenUrlKey() {
        final int MAX_RETRY_COUNT = 5;
        int count = 0;

        while(count ++ < MAX_RETRY_COUNT){
            String shortenUrlKey = ShortenUrl.generateShortenUrlKey();
            ShortenUrl shortenUrl = shortenUrlRepository.findShortenUrlByShortenUrlKey(
                    shortenUrlKey);

            if(shortenUrl == null){
                return shortenUrlKey;
            }
        }
        throw new LackOfShortenUrlKeyException();
    }

    /// 리다이렉트
    // 의도적으로 메서드 전체에 @Transactional을 걸지 않는다.
    // 조회/카운트 증가/클릭로그 저장을 각각 짧은 트랜잭션으로 분리해야
    // 클릭로그 저장 실패(예외를 삼킴)가 카운트 증가를 롤백시키지 않고, 인기 URL의 행 락 점유도 짧아진다.
    public String getOriginalUrlByShortenUrlKey(String shortenUrlKey){
        ShortenUrl shortenUrl = shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

        if(shortenUrl == null){
            throw new NotFoundShortenUrlException();
        }
        // 접속 횟수는 DB에서 원자적으로 증가 (읽기→더하기→저장 방식은 동시 요청에서 유실됨)
        shortenUrlRepository.incrementRedirectCount(shortenUrlKey);

        try {
            clickLogRepository.save(shortenUrlKey, LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        } catch (RuntimeException e) { }

        return shortenUrl.getOriginalUrl();
    }

    @Transactional(readOnly = true)
    public ClickLogPageResponseDto getClicks(String key, LocalDate from, LocalDate to,
                                             Long beforeId, int size) {
        // 기존의 키 확인(404) 코드
        if (!shortenUrlRepository.existsByShortenUrlKey(key)) {
            throw new NotFoundShortenUrlException();
        }
        int limit = Math.min(Math.max(size, 1), 50);
        LocalDateTime fromTime = (from == null) ? null : from.atStartOfDay();
        LocalDateTime toTime = (to == null) ? null : to.plusDays(1).atStartOfDay(); // 종료일 하루 전체 포함

        List<ClickLog> rows = clickLogRepository.findPage(key, fromTime, toTime, beforeId, limit + 1);
        boolean hasNext = rows.size() > limit;
        return ClickLogPageResponseDto.of(hasNext ? rows.subList(0, limit) : rows, hasNext);
    }

    @Transactional(readOnly = true)
    public ShortenUrlInformationDto getShortenUrlInforamtionByShortenUrlKey(String shortenUrlKey){
        ShortenUrl shortenUrl1 = shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

        if(shortenUrl1 == null){
            throw new NotFoundShortenUrlException();
        }

        ShortenUrlInformationDto shortenUrlInformationDto = new ShortenUrlInformationDto(shortenUrl1);

        return shortenUrlInformationDto;
    }

}
