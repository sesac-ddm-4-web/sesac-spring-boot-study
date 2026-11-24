package com.example.short_url.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaShortenUrlRepository extends JpaRepository<ShortenUrl, String> {
}
