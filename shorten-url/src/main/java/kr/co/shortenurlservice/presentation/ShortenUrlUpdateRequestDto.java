
package kr.co.shortenurlservice.presentation;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record ShortenUrlUpdateRequestDto(
        @NotBlank
        @URL(protocol = "https")
        String originalUrl
) {
}
