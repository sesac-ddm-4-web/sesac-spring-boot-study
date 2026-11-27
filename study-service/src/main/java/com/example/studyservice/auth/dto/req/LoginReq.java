package com.example.studyservice.auth.dto.req;

import jakarta.validation.constraints.NotBlank;

public record LoginReq(
        @NotBlank
        String email,

        @NotBlank
        String password
) {
}
