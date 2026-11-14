package com.example.short_url.presentation;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;

import java.util.List;

public class ShortenUrlCreateRequestDto {
    @NotEmpty
    private List<@URL(regexp = "[(http(s)?):\\/\\/(www\\.)?a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)") String> originalUrls;
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,20}$")
    private String customKey;
    private Integer expireDays;

    public ShortenUrlCreateRequestDto(){}

    public ShortenUrlCreateRequestDto(List<String> originalUrls, String customKey, Integer exprieDays){
        this.originalUrls = originalUrls;
        this.customKey = customKey;
        this.expireDays = exprieDays;
    }

    public List<String> getOriginalUrls() {
        return originalUrls;
    }

    public String getCustomKey() {
        return customKey;
    }

    public Integer getExpireDays() {
        return expireDays;
    }
}
