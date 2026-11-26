package com.example.personal_project.infrastructure.repository;

import com.example.personal_project.domain.item.model.CollectionItem;
import com.example.personal_project.domain.item.model.SourceType;
import com.example.personal_project.domain.item.repository.ItemRepository;
import com.example.personal_project.global.exception.DuplicationExternalProductException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.NoSuchElementException;

@Repository
public class DatabaseProductRepository implements ItemRepository {

    private List<CollectionItem> items;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public DatabaseProductRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public CollectionItem add(CollectionItem collectionItem) {
        String sql =
                """
                        INSERT INTO products
                            (name, price, source_type, external_id, stock, category, image)
                        VALUES
                            (:name, :price, :sourceType, :externalId, :stock, :category, :image)
                        """;

        var parameters = new MapSqlParameterSource()
                .addValue("name", collectionItem.name())
                .addValue("price", collectionItem.price())
                .addValue("sourceType", collectionItem.sourceType().name())
                .addValue("externalId", collectionItem.externalId())
                .addValue("stock", collectionItem.stock())
                .addValue("category", collectionItem.category())
                .addValue("image", collectionItem.image());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            namedParameterJdbcTemplate.update(sql, parameters, keyHolder, new String[]{"id"});
        } catch (DuplicateKeyException exception) {
            if (collectionItem.sourceType() == SourceType.DUMMY_JSON
                    && collectionItem.externalId() !=null) {
                throw new DuplicationExternalProductException();
            }
            throw exception;
        }

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("저장된 상품의 ID를 가져오지 못했습니다.");
        }

        return collectionItem.withId(key.longValue());
    }

    @Override
    public List<CollectionItem> findAll() {
        return namedParameterJdbcTemplate.query(
                """
                        SELECT id, name, price, source_type, external_id, stock, category, image
                        FROM products
                        ORDER BY id ASC
                        """,
                new DataClassRowMapper<>(CollectionItem.class)
        );
    }

    @Override
    public CollectionItem findById(Long id) {
        SqlParameterSource namedParameter = new MapSqlParameterSource("id", id);

        try {
            return namedParameterJdbcTemplate.queryForObject(
                    """
                            SELECT id, name, price, source_type, external_id, stock, category, image
                            FROM products
                            WHERE id = :id
                            """,
                    namedParameter, new DataClassRowMapper<>(CollectionItem.class));
        } catch (EmptyResultDataAccessException exception) {
            throw new NoSuchElementException("상품을 찾을 수 없습니다. ID: " + id);
        }
    }

    @Override
    public boolean existsBySourceTypeAndExternalId(SourceType sourceType, Long externalId) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("sourceType", sourceType.name())
                .addValue("externalId", externalId);

        Long count = namedParameterJdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM products
                        WHERE source_type = :sourceType
                            AND external_id = :externalId
                        """,
                namedParameters,
                Long.class
        );
        return count != null && count > 0;
    }

    public CollectionItem update(CollectionItem collectionItem) {
        SqlParameterSource namedParameter = new BeanPropertySqlParameterSource(collectionItem);

        namedParameterJdbcTemplate.update(
                """
                        UPDATE products
                        SET name=:name,
                         price=:price,
                         stock=:stock
                        WHERE id=:id
                        """,
                namedParameter
        );

        return collectionItem;
    }
}
