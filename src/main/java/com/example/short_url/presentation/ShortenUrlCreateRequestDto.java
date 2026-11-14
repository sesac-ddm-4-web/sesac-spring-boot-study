package com.example.short_url.presentation;

import com.example.short_url.domain.ShortenUrl;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

import java.util.List;

public class ShortenUrlCreateRequestDto {
    @NotEmpty
    private List<@URL(regexp = "[(http(s)?):\\/\\/(www\\.)?a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)") String> originalUrls;

    public ShortenUrlCreateRequestDto(){}

    public ShortenUrlCreateRequestDto(List<String> originalUrls){
        this.originalUrls = originalUrls;
    }

    public List<String> getOriginalUrls() {
        return originalUrls;
    }
}
