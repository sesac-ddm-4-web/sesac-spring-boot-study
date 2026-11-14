package com.example.wishlist.infrastructure;

import com.example.wishlist.domain.CollectionItem;
import com.example.wishlist.domain.CollectionItemRepository;
import com.example.wishlist.domain.SourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;

@Repository
public class DatabaseCollectionItemRepository implements CollectionItemRepository {

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public DatabaseCollectionItemRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate){
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public CollectionItem add(CollectionItem collectionItem){
        KeyHolder keyHolder = new GeneratedKeyHolder();
        BeanPropertySqlParameterSource namedParameter = new BeanPropertySqlParameterSource(collectionItem);
        namedParameter.registerSqlType("sourceType", Types.VARCHAR);

        namedParameterJdbcTemplate
                .update("INSERT INTO collection_items (name, price, source_type, external_id, stock) VALUES (:name, :price, :sourceType, :externalId, :stock)", namedParameter, keyHolder);

        Long generatedId = keyHolder.getKey().longValue();
        collectionItem.setId(generatedId);
        return collectionItem;
    }

    public CollectionItem findById(Long id) {
        SqlParameterSource namedParameter = new MapSqlParameterSource("id", id);

        try {
            return namedParameterJdbcTemplate.queryForObject(
                    "SELECT id, name, price, source_type, external_id, stock FROM collection_items WHERE id=:id",
                    namedParameter,
                    new BeanPropertyRowMapper<>(CollectionItem.class)
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public CollectionItem findByExternalId(SourceType sourceType, Long externalId){
        SqlParameterSource namedParameter = new MapSqlParameterSource()
                .addValue("sourceType", sourceType.name())
                .addValue("externalId", externalId);

        try {
            return namedParameterJdbcTemplate.queryForObject(
                    "SELECT id, name, price, source_type, external_id, stock FROM collection_items WHERE source_type=:sourceType AND external_id=:externalId",
                    namedParameter,
                    new BeanPropertyRowMapper<>(CollectionItem.class)
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<CollectionItem> findAll() {
        List<CollectionItem> collectionItems = namedParameterJdbcTemplate.query(
                "SELECT id, name, price, source_type, external_id, stock FROM collection_items ORDER BY id",
                new BeanPropertyRowMapper<>(CollectionItem.class)
        );

        return collectionItems;
    }

    public CollectionItem update(CollectionItem collectionItem) {
        SqlParameterSource namedParameter = new BeanPropertySqlParameterSource(collectionItem);

        namedParameterJdbcTemplate.update("UPDATE collection_items SET name = :name, price = :price WHERE id = :id", namedParameter);

        return collectionItem;
    }

    public boolean delete(Long id) {
        SqlParameterSource namedParameter = new MapSqlParameterSource("id", id);

        int deletedCount = namedParameterJdbcTemplate.update(
                "DELETE FROM collection_items WHERE id=:id",
                namedParameter
        );
        return deletedCount > 0;
    }
}
