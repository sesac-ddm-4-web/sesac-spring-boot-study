package com.example.personal_project.domain;

public record ShortLink(
        Long id,
        String code,
        Long sessionId,
        Long materialId,
        long redirectCount
) {
    public ShortLink {
        if ((sessionId == null) == (materialId == null)) {
            throw new IllegalArgumentException("회차 또는 자료와 연결되어야 합니다.");
        }

        if (redirectCount < 0) {
            throw new IllegalArgumentException("이동 횟수는 음수일 수 없습니다.");
        }
    }
}
