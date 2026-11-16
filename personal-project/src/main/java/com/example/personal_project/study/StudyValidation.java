package com.example.personal_project.study;

import java.net.URI;
import java.net.URISyntaxException;

public final class StudyValidation {
    private StudyValidation() {}
    public static String text(String value, int max, String label) {
        if (value == null || value.isBlank() || value.length() > max)
            throw new IllegalArgumentException(label + " 입력값을 확인해주세요.");
        return value.trim();
    }
    public static String optional(String value, int max) {
        if (value == null || value.isBlank()) return null;
        if (value.length() > max) throw new IllegalArgumentException("입력 가능한 길이를 초과했습니다.");
        return value.trim();
    }
    public static String url(String value) {
        text(value, 2048, "URL");
        try {
            URI uri = new URI(value);
            if ((!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null || uri.getRawUserInfo() != null || uri.getPort() > 65535)
                throw new IllegalArgumentException("http 또는 https 형식의 올바른 URL을 입력해주세요.");
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("올바른 URL을 입력해주세요.");
        }
        return value;
    }
    public static String query(String value) {
        if (value == null) return "";
        if (value.length() > 100) throw new IllegalArgumentException("검색어는 100자 이하여야 합니다.");
        return value.trim();
    }
    public static void positive(long id) {
        if (id < 1) throw new IllegalArgumentException("ID는 양수여야 합니다.");
    }
}
