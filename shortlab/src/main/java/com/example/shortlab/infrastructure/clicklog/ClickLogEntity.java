package com.example.shortlab.infrastructure.clicklog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "click_log")
public class ClickLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shorten_url_key", nullable = false, length = 16)
    private String shortenUrlKey;

    @Column(name = "clicked_at", nullable = false)
    private LocalDateTime clickedAt;

    protected ClickLogEntity() {
    }

    public ClickLogEntity(String shortenUrlKey, LocalDateTime clickedAt) {
        this.shortenUrlKey = shortenUrlKey;
        this.clickedAt = clickedAt;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getClickedAt() {
        return clickedAt;
    }
}