package com.example.personal_project.domain.url.repository;

import com.example.personal_project.domain.url.model.ShortLink;

public interface ShortLinkRepository {
    void insert(ShortLink shortLink);
    ShortLink findByCode(String code);
    int incrementRedirectCount(String code);
}
