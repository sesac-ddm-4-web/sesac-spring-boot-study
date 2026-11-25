package io.github.tkdgh816.brevlet.dto.url;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record CreateShortenUrlRequest(
        @NotNull
        @URL(regexp = "^https?://(?:[\\w-]+(?:\\.[\\w-]+)*\\.[a-zA-Z]{2,}|localhost|(?:\\d{1,3}\\.){3}\\d{1,3})(?::\\d{1,5})?(?:[/?#]\\S*)?$")
        @Size(max = 2048)
        String originalUrl,
        @Min(1) @Max(365) Integer expiresInDays
) {

}
