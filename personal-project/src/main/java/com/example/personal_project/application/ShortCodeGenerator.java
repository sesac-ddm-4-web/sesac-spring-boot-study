package com.example.personal_project.application;

import com.example.personal_project.domain.ShortenUrl;
import org.springframework.stereotype.Component;

@Component
public class ShortCodeGenerator {
    public String generate() {
        return ShortenUrl.generateShortenUrlKey();
    }
}
