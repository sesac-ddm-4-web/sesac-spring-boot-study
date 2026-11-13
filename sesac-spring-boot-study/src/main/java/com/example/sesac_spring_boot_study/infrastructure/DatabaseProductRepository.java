package com.example.sesac_spring_boot_study.infrastructure;

import com.example.sesac_spring_boot_study.domain.Product;
import com.example.sesac_spring_boot_study.domain.ProductRepository;
import com.example.sesac_spring_boot_study.domain.SourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;
import java.util.NoSuchElementException;

@Repository
public class DatabaseProductRepository implements ProductRepository {

    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public DatabaseProductRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate){
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public Product add(Product product){
        KeyHolder keyHolder = new GeneratedKeyHolder();
        BeanPropertySqlParameterSource namedParameter = new BeanPropertySqlParameterSource(product);
        namedParameter.registerSqlType("sourceType", Types.VARCHAR);

        namedParameterJdbcTemplate.update("INSERT INTO products (name, price, source_type, external_id, stock) VALUES (:name, :price, :sourceType, :externalId, :stock)",namedParameter,keyHolder);

        Long generatedId = keyHolder.getKey().longValue();
        product.setId(generatedId);
        return product;

    }

    public Product findById(Long id){
        SqlParameterSource namedParameter = new MapSqlParameterSource("id", id);

        try {
            return namedParameterJdbcTemplate.queryForObject(
                    "SELECT id, name, price, source_type AS sourceType, external_id AS externalId, stock FROM products WHERE id=:id",
                    namedParameter,
                    new BeanPropertyRowMapper<>(Product.class)
            );
        } catch (EmptyResultDataAccessException e) {
            throw new NoSuchElementException("상품을 찾을 수 없습니다. id=" + id);
        }
    }

    public List<Product> findAll(){
        List<Product> products = namedParameterJdbcTemplate.query(
                "SELECT * FROM products ORDER BY id",
                new BeanPropertyRowMapper<>(Product.class)
        );

        return products;
    }

    public Product update(Product product){
        SqlParameterSource namedParameter = new BeanPropertySqlParameterSource(product);

        namedParameterJdbcTemplate.update("UPDATE products SET name=:name, price=:price, stock=:stock WHERE id=:id", namedParameter);

        return product;
    }

    public boolean existsByExternalId(SourceType sourceType, Long externalId){
        SqlParameterSource namedParameter = new MapSqlParameterSource()
                .addValue("sourceType", sourceType.name())
                .addValue("externalId", externalId);

        Integer count = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products WHERE source_type = :sourceType AND external_id = :externalId",
                namedParameter,
                Integer.class);
        return count > 0;
    }

    public void delete(Long id){
        SqlParameterSource namedParameter = new MapSqlParameterSource("id", id);

        namedParameterJdbcTemplate.update(
                "DELETE FROM products WHERE id=:id",
                namedParameter
        );

    }
}
