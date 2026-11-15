package com.example.short_url.application;

import com.example.short_url.exception.*;
import com.example.short_url.domain.ShortenUrl;
import com.example.short_url.domain.ShortenUrlRepository;
import com.example.short_url.presentation.ShortenUrlCreateRequestDto;
import com.example.short_url.presentation.ShortenUrlCreateResponseDto;
import com.example.short_url.presentation.ShortenUrlInformationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SimpleShortenUrlService {

    private ShortenUrlRepository shortenUrlRepository;

    @Autowired
    SimpleShortenUrlService(ShortenUrlRepository shortenUrlRepository){
        this.shortenUrlRepository = shortenUrlRepository;
    }

    public ShortenUrlCreateResponseDto generateShortenUrl(ShortenUrlCreateRequestDto shortenUrlCreateRequestDto){

        List<String> originalUrls = shortenUrlCreateRequestDto.getOriginalUrls();
        String originalUrl = originalUrls.get(ThreadLocalRandom.current().nextInt(originalUrls.size()));

        Integer expireDays = shortenUrlCreateRequestDto.getExpireDays();
        String shortenUrlKey= getShortenUrlKey(shortenUrlCreateRequestDto.getCustomKey());
        LocalDateTime expire;

        if (expireDays == null) {
            expire = null;
        } else {
            LocalDateTime now = LocalDateTime.now();
            expire = now.plusDays(expireDays);
            //expire = now.plusSeconds(expireDays);
        }

        ShortenUrl shortenUrl = new ShortenUrl(originalUrl,shortenUrlKey, expire);
        shortenUrlRepository.saveShortenUrl(shortenUrl);

        ShortenUrlCreateResponseDto shortenUrlCreateResponseDto = new ShortenUrlCreateResponseDto(shortenUrl);
        return shortenUrlCreateResponseDto;
    }

    public ShortenUrlInformationDto getShortenUrlInformationByShortenUrlKey(String shortenUrlKey){
        ShortenUrl shortenUrl = shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

        if (null==shortenUrl) throw new NotFoundShortenUrlException();

        ShortenUrlInformationDto shortenUrlInformationDto = new ShortenUrlInformationDto(shortenUrl);

        return shortenUrlInformationDto;
    }

    public String getOriginalUrlByShortenUrlKey(String shortenUrlKey){
        ShortenUrl shortenUrl = shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

        if (null==shortenUrl) throw new NotFoundShortenUrlException();
        if (shortenUrl.isExpired()) throw new ExpiredShortenUrlException();
        shortenUrl.access();
        shortenUrl.increaseRedirectCount();
        shortenUrlRepository.saveShortenUrl(shortenUrl);

        String originalUrl = shortenUrl.getOriginalUrl();

        return originalUrl;
    }

    private String getUniqueShortenUrlKey(){
        final int MAX_RETRY_COUNT = 5;
        int count = 0;

        while(count++ <MAX_RETRY_COUNT){
            String shortenUrlKey = ShortenUrl.generateShortenUrlKey();
            ShortenUrl shortenUrl = shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

            if(null==shortenUrl) return shortenUrlKey;
        }

        throw new LackOfShortenUrlKeyException();
    }

    private String getShortenUrlKey(String customKey){
        if (customKey == null) {
            return getUniqueShortenUrlKey();
        }
        ShortenUrl exist = shortenUrlRepository.findShortenUrlByShortenUrlKey(customKey);
        if (exist != null && !exist.isExpired()) {
            throw new DuplicateShortenUrlKeyException();
        }
        return customKey;
    }

    public void expireShortenUrl(String shortenUrlKey, String managementToken){
        ShortenUrl shortenUrl = shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

        if (null==shortenUrl) throw new NotFoundShortenUrlException();
        if (!shortenUrl.TokenMatch(managementToken)) throw new InvalidManagementTokenException();

        shortenUrl.expireNow();
        shortenUrlRepository.saveShortenUrl(shortenUrl);
    }
}
