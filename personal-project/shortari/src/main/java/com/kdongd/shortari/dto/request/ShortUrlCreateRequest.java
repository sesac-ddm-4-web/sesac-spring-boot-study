package com.kdongd.shortari.dto.request;

import com.kdongd.shortari.domain.UrlMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ShortUrlCreateRequest(

        @NotBlank(message = "원본 URL은 필수입니다.")
        String originalUrl,

        @Size(max = 20, message = "표시 이름은 20자 이하여야 합니다.")
        String displayName,

        @NotNull(message = "URL 생성 방식을 선택해주세요.")
        UrlMode mode

) {
}