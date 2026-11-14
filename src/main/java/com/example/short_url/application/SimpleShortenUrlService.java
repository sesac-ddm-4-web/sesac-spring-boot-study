package com.example.short_url.application;

import com.example.short_url.LackOfShortenUrlKeyException;
import com.example.short_url.NotFoundShortenUrlException;
import com.example.short_url.domain.ShortenUrl;
import com.example.short_url.domain.ShortenUrlRepository;
import com.example.short_url.presentation.ShortenUrlCreateRequestDto;
import com.example.short_url.presentation.ShortenUrlCreateResponseDto;
import com.example.short_url.presentation.ShortenUrlInformatinDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SimpleShortenUrlService {

    private ShortenUrlRepository shortenUrlRepository;

    @Autowired
    SimpleShortenUrlService(ShortenUrlRepository shortenUrlRepository){
        this.shortenUrlRepository = shortenUrlRepository;
    }

    public ShortenUrlCreateResponseDto generateShortenUrl(ShortenUrlCreateRequestDto shortenUrlCreateRequestDto){
        String originalUrl = shortenUrlCreateRequestDto.getOriginalUrl();
        String shortenUrlKey= getUniqueShortenUrlKey();

        ShortenUrl shortenUrl = new ShortenUrl(originalUrl,shortenUrlKey);
        shortenUrlRepository.saveShortenUrl(shortenUrl);

        ShortenUrlCreateResponseDto shortenUrlCreateResponseDto = new ShortenUrlCreateResponseDto(shortenUrl);
        return shortenUrlCreateResponseDto;
    }

    public ShortenUrlInformatinDto getShortenUrlInformationByShortenUrlKey(String shortenUrlKey){
        ShortenUrl shortenUrl = shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

        if (null==shortenUrl) throw new NotFoundShortenUrlException();

        ShortenUrlInformatinDto shortenUrlInformatinDto = new ShortenUrlInformatinDto(shortenUrl);

        return shortenUrlInformatinDto;
    }

    public String getOriginalUrlByShortenUrlKey(String shortenUrlKey){
        ShortenUrl shortenUrl = shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

        if (null==shortenUrl) throw new NotFoundShortenUrlException();
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
}
