package com.example.shorten_url_service.infrastructure;

import jakarta.persistence.*;

@Entity
@Table(name = "pdf_files")
public class PdfFileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shorten_url_id", nullable = false)
    private ShortenUrlEntity shortenUrl;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false, unique = true)
    private String storedFileName;

    protected PdfFileEntity() {
    }

    public PdfFileEntity(ShortenUrlEntity shortenUrl,
                         String originalFileName,
                         String storedFileName) {
        this.shortenUrl = shortenUrl;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }
}