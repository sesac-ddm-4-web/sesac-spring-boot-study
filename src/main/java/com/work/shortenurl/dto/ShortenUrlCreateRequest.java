package com.work.shortenurl.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShortenUrlCreateRequest(
        @NotBlank @Size(max = 2048) String originalUrl,
        @NotBlank @Size(max = 50) String channel
) {}