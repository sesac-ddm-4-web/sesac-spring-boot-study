package com.example.studyservice.member.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpdateReq(
        @NotBlank
        @Size(max = 30)
        String nickname,

        @Size(max = 500)
        String bio,

        @Size(max = 200)
        String interests
) {
}
