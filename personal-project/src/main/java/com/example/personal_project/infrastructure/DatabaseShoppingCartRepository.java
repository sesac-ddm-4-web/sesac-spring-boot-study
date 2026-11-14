package com.example.personal_project.infrastructure;

import com.example.personal_project.domain.entity.ShoppingCart;
import com.example.personal_project.domain.exception.CommonConflictException;
import com.example.personal_project.domain.exception.CommonNotFoundException;
import com.example.personal_project.domain.repository.ShoppingCartRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Primary
public class DatabaseShoppingCartRepository implements ShoppingCartRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public DatabaseShoppingCartRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public ShoppingCart add(ShoppingCart shoppingCart) {
        String countSql = "SELECT COUNT(*) FROM shopping_carts";
        Integer count = namedParameterJdbcTemplate.queryForObject(countSql, new MapSqlParameterSource(), Integer.class);
        if (count >= ShoppingCart.MAX_CART_ITEM)
            throw new CommonConflictException("장바구니 아이템은 최대 20개입니다");

        String sql = """
            INSERT INTO shopping_carts
            (collection_item_id, name, unit_price, stock, item_type, amount)
            VALUES
            (:collectionItemId, :name, :unitPrice, :stock, :itemType, :amount)
            """;

        SqlParameterSource params = new BeanPropertySqlParameterSource(shoppingCart);
        namedParameterJdbcTemplate.update(sql, params);
        return shoppingCart;
    }

    @Override
    public Boolean existByCollectionItemId(Long collectionItemId) {
        String sql = """
            SELECT EXISTS(
                SELECT 1
                FROM shopping_carts
                WHERE collection_item_id = :collectionItemId
            )
            """;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("collectionItemId",collectionItemId);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Boolean.class);
    }

    @Override
    public ShoppingCart findByCollectionItemId(Long collectionItemId) {
        String sql = """
            SELECT collection_item_id, name, unit_price, stock, item_type, amount
            FROM shopping_carts
            WHERE collection_item_id = :collectionItemId
            """;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("collectionItemId",collectionItemId);
        try {
            return namedParameterJdbcTemplate.queryForObject(sql, params, new BeanPropertyRowMapper<>(ShoppingCart.class));
        } catch (EmptyResultDataAccessException e) {
            throw new CommonNotFoundException("장바구니에 해당 item이 존재하지 않습니다");
        }
    }

    @Override
    public ShoppingCart update(ShoppingCart shoppingCart) {
        String sql = """
            UPDATE shopping_carts
            SET amount = :amount
            WHERE collection_item_id = :collectionItemId
            """;
        SqlParameterSource params = new BeanPropertySqlParameterSource(shoppingCart);
        namedParameterJdbcTemplate.update(sql, params);
        return shoppingCart;
    }

    @Override
    public List<ShoppingCart> findAll() {
        String sql = """
            SELECT collection_item_id, name, unit_price,
                   stock, item_type, amount
            FROM shopping_carts
            """;
        return namedParameterJdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ShoppingCart.class));
    }

    @Override
    public void delete(Long collectionItemId) {
        String sql = """
            DELETE FROM shopping_carts
            WHERE collection_item_id = :collectionItemId
            """;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("collectionItemId",collectionItemId);

        int deletedRows = namedParameterJdbcTemplate.update(sql, params);
        if (deletedRows == 0)
            throw new CommonNotFoundException("장바구니에 해당 item이 존재하지 않습니다");
    }
}
