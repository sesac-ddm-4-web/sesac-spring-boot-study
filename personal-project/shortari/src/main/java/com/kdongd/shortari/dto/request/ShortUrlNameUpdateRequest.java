package com.kdongd.shortari.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShortUrlNameUpdateRequest(

        @NotBlank(message = "표시 이름을 입력해주세요.")
        @Size(max = 20, message = "표시 이름은 최대 20자입니다.")
        String displayName
) {
}
