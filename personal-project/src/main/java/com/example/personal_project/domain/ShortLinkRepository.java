package com.example.personal_project.domain;

public interface ShortLinkRepository {
    void insert(ShortLink shortLink);
    ShortLink findByCode(String code);
    int incrementRedirectCount(String code);
}
