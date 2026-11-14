package com.example.personal_project.infrastructure;

import com.example.personal_project.domain.entity.CollectionItem;
import com.example.personal_project.domain.exception.CommonNotFoundException;
import com.example.personal_project.domain.repository.CollectionItemRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Primary
public class DatabaseCollectionItemRepository implements CollectionItemRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public DatabaseCollectionItemRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public CollectionItem add(CollectionItem collectionItem) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        SqlParameterSource namedParameter = new BeanPropertySqlParameterSource(collectionItem);
        namedParameterJdbcTemplate.update("INSERT INTO collection_items (name,price,item_type,external_id,stock) VALUES (:name,:price,:itemType,:externalId,:stock)",namedParameter,keyHolder);
        Long generatedId = keyHolder.getKey().longValue();
        collectionItem.setId(generatedId);
        return collectionItem;
    }

    @Override
    public List<CollectionItem> findAll() {
        List<CollectionItem> collectionItems = namedParameterJdbcTemplate.query(
                "SELECT * FROM collection_items",
                new BeanPropertyRowMapper<>(CollectionItem.class));
        return collectionItems;
    }

    @Override
    public CollectionItem findById(Long id) {
        SqlParameterSource namedParameter = new MapSqlParameterSource("id",id);

        try {
            CollectionItem collectionItem = namedParameterJdbcTemplate.queryForObject(
                    "SELECT id,name,price,item_type,external_id,stock FROM collection_items WHERE id=:id",
                    namedParameter,
                    new BeanPropertyRowMapper<>(CollectionItem.class));
            return collectionItem;
        }catch (EmptyResultDataAccessException e){
            throw new CommonNotFoundException("해당 id로 item을 찾을 수 없습니다");
        }
    }

    @Override
    public Boolean existsByExternalId(Long externalId) {
        SqlParameterSource namedParameter = new MapSqlParameterSource("externalId",externalId);

        return namedParameterJdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM collection_items WHERE external_id = :externalId)",
                namedParameter,
                Boolean.class);
    }

    @Override
    public List<CollectionItem> findItemsWithBudget(Integer budget, Integer limit) {
        String sql = """
            SELECT *
            FROM collection_items
            WHERE price <= :budget
            ORDER BY price ASC, id ASC
            LIMIT :limit
            """;

        SqlParameterSource namedParameter = new MapSqlParameterSource()
                .addValue("budget", budget)
                .addValue("limit", limit);

        return namedParameterJdbcTemplate.query(sql, namedParameter, new BeanPropertyRowMapper<>(CollectionItem.class));
    }
}
