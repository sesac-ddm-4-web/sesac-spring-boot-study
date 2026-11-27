package com.example.shorten_url_service.presentation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;
import java.net.URI;
import org.hibernate.validator.constraints.URL;

// 생성 요청 DTO
// 스프링이 요청 JSON을 읽을 때 원본 주소 받기
public class ShortenUrlCreateRequestDto {
    @Size(max = 2048)
    @NotNull
    @URL(regexp = "(?i)https?://.+")
    private String originalUrl; // 사용자가 보낸 값(원본 주소)

    public ShortenUrlCreateRequestDto() {
    }

    public ShortenUrlCreateRequestDto(String originalUrl) {
        this.originalUrl = originalUrl;
    }
    @AssertTrue(message = "인코딩한 URL은 2,048자 이하여야 합니다.")
    private boolean isRedirectUriValid() {
        if (originalUrl == null || originalUrl.length() > 2048) {
            return true; // @NotNull과 @Size에서 검사한다.
        }
        try {
            return URI.create(originalUrl).toASCIIString().length() <= 2048;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public String getOriginalUrl() {
        return originalUrl;
    }
}
