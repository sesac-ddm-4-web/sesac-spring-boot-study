package com.example.personal_project.domain.url.model;

import java.net.URI;
import java.net.URISyntaxException;

public record OriginalUrl(String originalUrl) {
    public OriginalUrl {
        if (originalUrl == null || originalUrl.isBlank()) {
            throw new IllegalArgumentException("원본 URL을 입력해주세요.");
        }

        if (originalUrl.length() > 2048) {
            throw new IllegalArgumentException("원본 URL은 2048자 이하여야 합니다.");
        }

        URI uri;
        try {
            uri = new URI(originalUrl);
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("올바른 URL 형식으로 입력해주세요.", exception);
        }

        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("http 또는 https 주소만 등록할 수 있습니다.");
        }

        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("호스트가 포함된 올바른 URL을 입력해주세요.");
        }

        if (uri.getRawUserInfo() != null) {
            throw new IllegalArgumentException("사용자 정보가 포함된 URL은 등록할 수 없습니다.");
        }

        if (uri.getPort() > 65535) {
            throw new IllegalArgumentException("올바른 포트 번호를 입력해주세요.");
        }
    }

}
