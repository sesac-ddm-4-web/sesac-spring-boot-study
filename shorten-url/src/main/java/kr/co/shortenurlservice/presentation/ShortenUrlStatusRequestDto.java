package kr.co.shortenurlservice.presentation;

import jakarta.validation.constraints.NotNull;

public record ShortenUrlStatusRequestDto(
        @NotNull Boolean active
) {}