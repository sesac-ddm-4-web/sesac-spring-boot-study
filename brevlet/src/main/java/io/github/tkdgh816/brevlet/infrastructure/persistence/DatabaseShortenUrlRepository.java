package io.github.tkdgh816.brevlet.infrastructure.persistence;

import io.github.tkdgh816.brevlet.domain.ItemNotFoundException;
import io.github.tkdgh816.brevlet.domain.key.ShortenUrlKeyConflictException;
import io.github.tkdgh816.brevlet.domain.url.ShortenUrl;
import io.github.tkdgh816.brevlet.domain.url.ShortenUrlNotFoundException;
import io.github.tkdgh816.brevlet.domain.url.ShortenUrlRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import org.hibernate.exception.ConstraintViolationException;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.criteria.Expressions;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Profile({"mysql", "h2"})
@Repository
public class DatabaseShortenUrlRepository implements ShortenUrlRepository {
    private final JpaShortenUrlRepository jpaShortenUrlRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public DatabaseShortenUrlRepository(JpaShortenUrlRepository jpaShortenUrlRepository) {
        this.jpaShortenUrlRepository = jpaShortenUrlRepository;
    }

    @Override
    @Transactional
    public void create(ShortenUrl shortenUrl) {
        try {
            entityManager.persist(ShortenUrlEntity.from(shortenUrl));
            entityManager.flush();
        } catch (ConstraintViolationException ex) {
            if (ex.getKind() == ConstraintViolationException.ConstraintKind.UNIQUE) {
                throw new ShortenUrlKeyConflictException("이미 존재하는 단축 URL 키입니다.");
            }
            throw ex;
        }
    }

    @Override
    public Optional<ShortenUrl> findByKey(String shortenUrlKey) {
        return jpaShortenUrlRepository.findById(shortenUrlKey)
                .map(ShortenUrlEntity::toDomain);
    }

    @Override
    public void increaseRedirectCount(String shortenUrlKey) {
        long updatedRows = jpaShortenUrlRepository.update((root, update, builder) -> {
            var key = Expressions.path(root, ShortenUrlEntity::getShortenUrlKey);
            var count = Expressions.path(root, ShortenUrlEntity::getRedirectCount);
            update.set(count, builder.sum(count, 1L));
            return builder.equal(key, shortenUrlKey);
        });
        if (updatedRows == 0) {
            throw new ShortenUrlNotFoundException("단축 URL(%s)를 찾을 수 없습니다.".formatted(shortenUrlKey));
        }
    }
}
