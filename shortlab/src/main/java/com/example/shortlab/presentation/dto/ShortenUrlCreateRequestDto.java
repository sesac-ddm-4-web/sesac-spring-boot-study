package com.example.shortlab.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public class ShortenUrlCreateRequestDto {

    @NotBlank
    @Size(max = 2048)
    @URL(regexp = "^(http|https)://.+$")
    private String originalUrl;

    @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9-]{2,19}$", message = "별칭 형식이 올바르지 않습니다.")
    private String alias;   // 선택 항목: 없으면 null

    public String getAlias() { return alias; }

    public ShortenUrlCreateRequestDto() {}

    public ShortenUrlCreateRequestDto(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

}