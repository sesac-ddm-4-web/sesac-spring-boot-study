package com.kdongd.shortari.service;

import com.kdongd.shortari.exception.InvalidUrlException;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

@Component
public class UrlNormalizer {

    public String normalize(String originalUrl) {

        if (originalUrl == null || originalUrl.isBlank()) {
            throw new InvalidUrlException();
        }

        String normalized = originalUrl.trim();

        if (!normalized.matches("(?i)^https?://.*")) {

            if (normalized.contains("://")) {
                throw new InvalidUrlException();
            }

            normalized = "https://" + normalized;
        }

        try {
            URI uri = new URI(normalized);

            String scheme = uri.getScheme();
            String host = uri.getHost();

            if (scheme == null || host == null) {
                throw new InvalidUrlException();
            }

            if (!scheme.equalsIgnoreCase("http")
                    && !scheme.equalsIgnoreCase("https")) {
                throw new InvalidUrlException();
            }

            if (!host.contains(".")) {
                throw new InvalidUrlException();
            }

            if (uri.getUserInfo() != null
                    || uri.getPort() > 65535) {
                throw new InvalidUrlException();
            }

            String lowerHost = host.toLowerCase(Locale.ROOT);

            if (lowerHost.equals("localhost")
                    || lowerHost.endsWith(".localhost")
                    || lowerHost.endsWith(".local")
                    || lowerHost.endsWith(".internal")) {
                throw new InvalidUrlException();
            }

            return uri.toString();

        } catch (URISyntaxException e) {
            throw new InvalidUrlException();
        }
    }
}