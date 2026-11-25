package com.example.shortlab.application;

import com.example.shortlab.domain.exception.NotFoundShortenUrlException;
import com.example.shortlab.presentation.dto.ShortenUrlCreateRequestDto;
import com.example.shortlab.presentation.dto.ShortenUrlCreateResponseDto;
import com.example.shortlab.presentation.dto.ShortenUrlInformationDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SimpleShortenUrlServiceTest {

    @Autowired
    private SimpleShortenUrlService simpleShortenUrlService;

    @Test
    @DisplayName("URL을 단축한 후 단축된 URL 키로 조회하면 원래 URL이 조회되어야 한다.")
    void shortenUrlAddTest(){
        // given
        String originalUrl = "https://www.google.com";
        ShortenUrlCreateRequestDto requestDto = new ShortenUrlCreateRequestDto(originalUrl);

        // when
        ShortenUrlCreateResponseDto responseDto =
                simpleShortenUrlService.generateShortenUrl(requestDto);
        String shortenUrlKey = responseDto.getShortenUrlKey();
        String foundOriginalUrl =
                simpleShortenUrlService.getOriginalUrlByShortenUrlKey(shortenUrlKey);

        // then
        assertEquals(originalUrl, responseDto.getOriginalUrl());
        assertEquals(originalUrl, foundOriginalUrl);
    }

    @Test
    @DisplayName("존재하지 않는 단축 URL을 조회하는 경우 예외가 터져야한다.")
    void notExistShortenUrlExceptionTest(){
        // 'O', 'I'는 base56 문자 집합에 없어서 생성될 수 없는 키
        String notExistKey = "NOTEXIST";

        assertThrows(NotFoundShortenUrlException.class,
                () -> simpleShortenUrlService.getOriginalUrlByShortenUrlKey(notExistKey));
    }
}