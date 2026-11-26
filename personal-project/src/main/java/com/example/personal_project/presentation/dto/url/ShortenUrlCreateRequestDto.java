package com.example.personal_project.presentation.dto.url;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ShortenUrlCreateRequestDto {

    @NotBlank(message = "원본 URL을 입력해주세요.")
    @Size(max = 2048, message = "원본 URL은 2048자 이하여야 합니다.")
    private String originalUrl;
    private String code;

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getCode() {
        return code;
    }
}
