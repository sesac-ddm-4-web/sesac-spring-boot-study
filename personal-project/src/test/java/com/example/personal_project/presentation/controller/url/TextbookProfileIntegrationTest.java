package com.example.personal_project.presentation.controller.url;

import com.example.personal_project.domain.url.model.ShortenUrl;
import com.example.personal_project.domain.url.repository.ShortenUrlRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("textbook")
class TextbookProfileIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ShortenUrlRepository repository;

    @Test
    void textbookProfileHasOneRedirectMappingAndUsesLegacyRepository() throws Exception {
        String code = "profile-" + UUID.randomUUID().toString().replace("-", "");
        repository.saveShortenUrlIfAbsent(new ShortenUrl("https://example.com/textbook", code));
        mvc.perform(get("/study/{code}", code))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com/textbook"))
                .andExpect(header().string("Cache-Control", "no-store"));
    }
}
