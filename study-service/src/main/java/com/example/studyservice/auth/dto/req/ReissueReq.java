package com.example.studyservice.auth.dto.req;

import jakarta.validation.constraints.NotBlank;

public record ReissueReq(
        @NotBlank
        String refreshToken
) {
}
