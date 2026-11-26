package com.example.personal_project.infrastructure.repository.url;

import com.example.personal_project.domain.url.exception.ShortLinkCodeConflictException;
import com.example.personal_project.domain.url.model.ShortLink;
import com.example.personal_project.domain.url.repository.ShortLinkRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

@Repository
public class JpaShortLinkRepository implements ShortLinkRepository {

    private static final int MYSQL_DUPLICATE_KEY_ERROR = 1062;
    private static final String CODE_CONSTRAINT = "uk_short_links_code";

    private final SpringDataShortLinkRepository repository;

    public JpaShortLinkRepository(SpringDataShortLinkRepository repository) {
        this.repository = repository;
    }

    @Override
    public void insert(ShortLink shortLink) {
        try {
            repository.saveAndFlush(new ShortLinkEntity(shortLink));
        } catch (DataIntegrityViolationException exception) {
            throw translateIntegrityViolation(exception);
        }
    }

    @Override
    public ShortLink findByCode(String code) {
        return repository.findByCode(code).map(ShortLinkEntity::toDomain).orElse(null);
    }

    @Override
    public int incrementRedirectCount(String code) {
        return repository.incrementRedirectCount(code);
    }

    private RuntimeException translateIntegrityViolation(DataIntegrityViolationException exception) {
        for (Throwable cause = exception.getCause(); cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation
                    && violation.getErrorCode() == MYSQL_DUPLICATE_KEY_ERROR) {
                String name = violation.getConstraintName();
                if (name != null && CODE_CONSTRAINT.equals(name.substring(name.lastIndexOf('.') + 1))) {
                    return new ShortLinkCodeConflictException(exception);
                }
                return new DuplicateKeyException(exception.getMessage(), exception);
            }
        }
        return exception;
    }
}
