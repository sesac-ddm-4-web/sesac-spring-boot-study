package com.example.shorturl.link.dto.req;

import jakarta.validation.constraints.NotNull;

public record ShortUrlStatusReq(
        @NotNull
        Boolean active
) {
}
