package com.example.demo.item.repository;

import com.example.demo.global.exception.EntityNotFoundException;
import com.example.demo.item.domain.CollectionItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
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
@Profile("dev")
public class DatabaseCollectionItemRepository implements CollectionItemRepository{

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public DatabaseCollectionItemRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate){
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public CollectionItem add(CollectionItem collectionItem){
        KeyHolder keyHolder = new GeneratedKeyHolder();
        BeanPropertySqlParameterSource namedParameter = new BeanPropertySqlParameterSource(collectionItem);
        namedParameter.registerSqlType("sourceType", Types.VARCHAR);

        namedParameterJdbcTemplate.update("INSERT INTO collection_items (name, price, source_type, external_id, stock) VALUES (:name, :price, :sourceType, :externalId, :stock)", namedParameter, keyHolder);

        Long generatedId = keyHolder.getKey().longValue();
        collectionItem.setId(generatedId);

        return collectionItem;
    }

    public CollectionItem findById(Long id){
        SqlParameterSource namedParameter = new MapSqlParameterSource("id", id);

        try{
            return namedParameterJdbcTemplate.queryForObject(
                    "SELECT id, name, price, source_type, external_id, stock FROM collection_items WHERE id= :id",
                    namedParameter,
                    new BeanPropertyRowMapper<>(CollectionItem.class)
            );
        } catch (EmptyResultDataAccessException e){
            throw new EntityNotFoundException("Item을 찾지 못했습니다.");
        }
    }

    public List<CollectionItem> findAll(){
        List<CollectionItem> collectionItems = namedParameterJdbcTemplate.query(
                "SELECT * FROM collection_items",
                new BeanPropertyRowMapper<>(CollectionItem.class)
        );

        return collectionItems;
    }

    public CollectionItem update(CollectionItem collectionItem){
        SqlParameterSource namedParameter = new BeanPropertySqlParameterSource(collectionItem);

        namedParameterJdbcTemplate.update("UPDATE collection_items SET name = :name, price = :price WHERE id = :id", namedParameter);

        return collectionItem;
    }

    public void delete(Long id){
        SqlParameterSource namedParameter = new MapSqlParameterSource("id", id);

        int deleted = namedParameterJdbcTemplate.update(
                "DELETE FROM collection_items WHERE id = :id",
                namedParameter
        );

        if(deleted == 0){
            throw new EntityNotFoundException("Item을 찾지 못했습니다.");
        }
    }

    @Override
    public boolean existsByExternalId(Long externalId) {
        SqlParameterSource namedParameter = new MapSqlParameterSource("externalId", externalId);

        Integer count = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM collection_items WHERE external_id = :externalId",
                namedParameter,
                Integer.class
        );
        return count != null && count > 0;
    }
}
