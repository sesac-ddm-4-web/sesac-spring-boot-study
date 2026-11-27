package com.example.personal_project.member.domain;

import com.example.personal_project.common.DomainText;
import com.example.personal_project.common.InvalidDomainFieldException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

@Entity
@Table(name = "members", uniqueConstraints = @UniqueConstraint(name = Member.EMAIL_UNIQUE_CONSTRAINT, columnNames = "email"))
public class Member {

    public static final String EMAIL_UNIQUE_CONSTRAINT = "uk_members_email";
    private static final int EMAIL_MAX_LENGTH = 254;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[^\\s@]+@[^\\s@.]+(?:\\.[^\\s@.]+)+", Pattern.UNICODE_CHARACTER_CLASS);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = EMAIL_MAX_LENGTH)
    private String email;

    @Column(name = "created_at", nullable = false, updatable = false, secondPrecision = 6)
    private Instant createdAt;

    protected Member() {
    }

    private Member(String email, Instant now) {
        this.email = normalizeEmail(email);
        this.createdAt = Objects.requireNonNull(now, "now").truncatedTo(ChronoUnit.MICROS);
    }

    public static Member register(String email, Instant now) {
        return new Member(email, now);
    }

    private static String normalizeEmail(String email) {
        String value = email == null ? null : email.toLowerCase(Locale.ROOT);
        String normalized = DomainText.requireLength(value, "email", 1, EMAIL_MAX_LENGTH);
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new InvalidDomainFieldException("email", "email format is invalid");
        }
        return normalized;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
