package com.sesac.studyhub.member.dto;

import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record ProfileUpdateRequest(
    @URL(message = "이미지 URL 형식이 올바르지 않습니다.")
    @Size(max = 255, message = "이미지 URL은 255자 이하여야 합니다.")
    String imageUrl,

    @Size(max = 500, message = "자기소개는 500자 이하여야 합니다.")
    String description
) { }