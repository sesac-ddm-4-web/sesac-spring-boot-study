package com.example.shorturl.link.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ShortUrlCreateReq(
        @NotBlank
        @Size(max = 2048)
        String originalUrl,

        String customKey,

        @Size(max = 50)
        String title,

        LocalDate expiresOn
) {
}
