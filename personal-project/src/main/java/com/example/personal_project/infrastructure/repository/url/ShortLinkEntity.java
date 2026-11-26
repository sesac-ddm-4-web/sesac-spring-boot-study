package com.example.personal_project.infrastructure.repository.url;

import com.example.personal_project.domain.url.model.ShortLink;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "short_links")
public class ShortLinkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "material_id")
    private Long materialId;

    @Column(name = "redirect_count", nullable = false)
    private long redirectCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected ShortLinkEntity() {
    }

    public ShortLinkEntity(ShortLink link) {
        this.code = link.code();
        this.sessionId = link.sessionId();
        this.materialId = link.materialId();
        this.redirectCount = 0;
        this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public ShortLink toDomain() {
        return new ShortLink(id, code, sessionId, materialId, redirectCount);
    }
}
