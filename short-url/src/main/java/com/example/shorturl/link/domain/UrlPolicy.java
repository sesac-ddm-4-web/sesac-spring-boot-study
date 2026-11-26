package com.example.shorturl.link.domain;

import com.example.shorturl.global.exception.ApiException;
import com.example.shorturl.global.exception.ErrorCode;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class UrlPolicy {

    public static final int MAX_URL_LENGTH = 2048;
    public static final String CUSTOM_KEY_REGEX = "[A-Za-z0-9_-]{4,30}";

    private static final Pattern CUSTOM_KEY_PATTERN = Pattern.compile(CUSTOM_KEY_REGEX);
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");
    private static final Set<String> RESERVED_KEYS = Set.of(
            "api", "admin", "index", "error", "static", "assets", "favicon", "css", "js", "images");

    private UrlPolicy() {
    }

    public static String normalizeOriginalUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new ApiException(ErrorCode.ORIGINAL_URL_REQUIRED);
        }
        String url = rawUrl.strip();
        if (url.length() > MAX_URL_LENGTH) {
            throw new ApiException(ErrorCode.ORIGINAL_URL_TOO_LONG, MAX_URL_LENGTH);
        }
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new ApiException(ErrorCode.INVALID_URL_FORMAT);
        }
        String scheme = uri.getScheme();
        if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase(Locale.ROOT))) {
            throw new ApiException(ErrorCode.UNSUPPORTED_URL_SCHEME);
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new ApiException(ErrorCode.MISSING_URL_HOST);
        }
        return url;
    }

    // 자기 자신 주소를 등록하면 무한 이동함
    public static void validateNotSelfLink(String originalUrl, URI serviceOrigin) {
        URI target = URI.create(originalUrl);
        if (target.getHost().equalsIgnoreCase(serviceOrigin.getHost()) && port(target) == port(serviceOrigin)) {
            throw new ApiException(ErrorCode.SELF_REFERENCING_URL);
        }
    }

    private static int port(URI uri) {
        if (uri.getPort() != -1) {
            return uri.getPort();
        }
        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }

    public static String validateCustomKey(String rawKey) {
        String key = rawKey.strip();
        if (!CUSTOM_KEY_PATTERN.matcher(key).matches()) {
            throw new ApiException(ErrorCode.INVALID_CUSTOM_KEY);
        }
        if (RESERVED_KEYS.contains(key.toLowerCase(Locale.ROOT))) {
            throw new ApiException(ErrorCode.RESERVED_CUSTOM_KEY, key);
        }
        return key;
    }
}
