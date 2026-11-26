package kr.co.shortenurlservice.infrastructure;

import jakarta.persistence.EntityManager;
import kr.co.shortenurlservice.domain.ShortenUrl;
import kr.co.shortenurlservice.domain.ShortenUrlRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public class JpaShortenUrlRepository implements ShortenUrlRepository {

    private static final int MYSQL_DUPLICATE_ENTRY = 1062;

    private final EntityManager entityManager;

    public JpaShortenUrlRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void saveShortenUrl(ShortenUrl shortenUrl) {
        try {
            entityManager.persist(shortenUrl);
            entityManager.flush();
        } catch (ConstraintViolationException exception) {
            if (exception.getErrorCode() == MYSQL_DUPLICATE_ENTRY) {
                throw new DuplicateKeyException("Duplicate shorten URL key", exception);
            }
            throw exception;
        }
    }

    @Override
    public ShortenUrl findShortenUrlByShortenUrlKey(String key) {
        return entityManager.find(ShortenUrl.class, key);
    }

    @Override
    public List<ShortenUrl> findAll(String keyword, int limit, int offset) {
        return entityManager.createQuery("""
                        SELECT url FROM ShortenUrl url
                        WHERE url.originalUrl LIKE :pattern ESCAPE :escapeChar
                        ORDER BY url.shortenUrlKey
                        """, ShortenUrl.class)
                .setParameter("pattern", "%" + keyword + "%")
                .setParameter("escapeChar", '\\')
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public long count(String keyword) {
        return entityManager.createQuery("""
                        SELECT COUNT(url) FROM ShortenUrl url
                        WHERE url.originalUrl LIKE :pattern ESCAPE :escapeChar
                        """, Long.class)
                .setParameter("pattern", "%" + keyword + "%")
                .setParameter("escapeChar", '\\')
                .getSingleResult();
    }

    @Override
    @Transactional
    public void updateStatus(String key, boolean active) {
        entityManager.createQuery("""
                        UPDATE ShortenUrl url SET url.active = :active
                        WHERE url.shortenUrlKey = :key
                        """)
                .setParameter("active", active)
                .setParameter("key", key)
                .executeUpdate();
    }

    @Override
    @Transactional
    public boolean increaseRedirectCount(String key) {
        return entityManager.createQuery("""
                        UPDATE ShortenUrl url
                        SET url.redirectCount = url.redirectCount + 1
                        WHERE url.shortenUrlKey = :key AND url.active = true
                        """)
                .setParameter("key", key)
                .executeUpdate() == 1;
    }

    @Override
    @Transactional
    public boolean updateOriginalUrl(String key, String originalUrl) {
        return entityManager.createQuery("""
                        UPDATE ShortenUrl url SET url.originalUrl = :originalUrl
                        WHERE url.shortenUrlKey = :key
                        """)
                .setParameter("originalUrl", originalUrl)
                .setParameter("key", key)
                .executeUpdate() == 1;
    }
}
