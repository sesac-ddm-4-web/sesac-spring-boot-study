package com.work.shortenurl.service;

import com.work.shortenurl.config.AppProperties;
import com.work.shortenurl.exception.InvalidUrlException;
import com.work.shortenurl.exception.SelfReferenceUrlException;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

@Component
public class UrlValidator {

    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    private final String serviceHost;

    public UrlValidator(AppProperties appProperties) {
        this.serviceHost = URI.create(appProperties.baseUrl()).getHost();
    }

    public void validate(String url) {
        URI uri = parse(url);

        String scheme = uri.getScheme();
        if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase())) {
            throw new InvalidUrlException("http 또는 https로 시작하는 URL만 단축할 수 있습니다.");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new InvalidUrlException("도메인이 없는 URL입니다.");
        }

        if (host.equalsIgnoreCase(serviceHost)) {
            throw new SelfReferenceUrlException();
        }
    }

    private URI parse(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new InvalidUrlException("URL 형식이 올바르지 않습니다.");
        }
    }
}
