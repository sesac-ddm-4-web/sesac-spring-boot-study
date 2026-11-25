package com.example.shortlab.application;

import com.example.shortlab.domain.ShortenUrl;
import com.example.shortlab.domain.ShortenUrlRepository;
import com.example.shortlab.domain.exception.LackOfShortenUrlKeyException;
import com.example.shortlab.presentation.dto.ShortenUrlCreateRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimpleShortenUrlServiceTest2 {

    @Mock
    private ShortenUrlRepository shortenUrlRepository;

    @InjectMocks
    private SimpleShortenUrlService simpleShortenUrlService;

    @Test
    @DisplayName("단축 URL 키가 계속 중복되어 재시도 횟수를 초과하면 예외가 터져야 한다.")
    void lackOfShortenUrlKeyExceptionTest(){
        // given: 어떤 키로 조회해도 이미 존재하는 것으로 응답
        ShortenUrl existingShortenUrl = new ShortenUrl("https://www.google.com", "aaaaaaaa");
        when(shortenUrlRepository.findShortenUrlByShortenUrlKey(anyString()))
                .thenReturn(existingShortenUrl);

        ShortenUrlCreateRequestDto requestDto =
                new ShortenUrlCreateRequestDto("https://www.naver.com");

        // when & then
        assertThrows(LackOfShortenUrlKeyException.class,
                () -> simpleShortenUrlService.generateShortenUrl(requestDto));

        // 최대 재시도 5번만큼 조회했고, 저장은 한 번도 일어나지 않아야 한다
        verify(shortenUrlRepository, times(5)).findShortenUrlByShortenUrlKey(anyString());
        verify(shortenUrlRepository, never()).saveShortenUrl(any(ShortenUrl.class));
    }

    @Test
    @DisplayName("키가 한 번 중복되더라도 재시도해서 정상적으로 저장되어야 한다.")
    void retryWhenDuplicatedKeyTest(){
        // given: 첫 조회는 중복(존재), 두 번째 조회는 없음(null)
        ShortenUrl existingShortenUrl = new ShortenUrl("https://www.google.com", "aaaaaaaa");
        when(shortenUrlRepository.findShortenUrlByShortenUrlKey(anyString()))
                .thenReturn(existingShortenUrl)
                .thenReturn(null);

        ShortenUrlCreateRequestDto requestDto =
                new ShortenUrlCreateRequestDto("https://www.naver.com");

        // when
        simpleShortenUrlService.generateShortenUrl(requestDto);

        // then
        verify(shortenUrlRepository, times(2)).findShortenUrlByShortenUrlKey(anyString());
        verify(shortenUrlRepository, times(1)).saveShortenUrl(any(ShortenUrl.class));
    }
}