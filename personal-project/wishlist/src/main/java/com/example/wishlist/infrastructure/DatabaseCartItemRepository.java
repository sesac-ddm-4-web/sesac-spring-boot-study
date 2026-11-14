package com.example.wishlist.infrastructure;

import com.example.wishlist.domain.CartItem;
import com.example.wishlist.domain.CartItemRepository;
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

import java.util.List;

@Repository
public class DatabaseCartItemRepository implements CartItemRepository {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public DatabaseCartItemRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate){
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public CartItem add(CartItem cartItem){
        KeyHolder keyHolder = new GeneratedKeyHolder();
        SqlParameterSource namedParameter = new BeanPropertySqlParameterSource(cartItem);

        namedParameterJdbcTemplate
                .update("INSERT INTO cart_items (wish_item_id, name, unit_price, stock, quantity) VALUES (:wishItemId, :name, :unitPrice, :stock, :quantity)", namedParameter, keyHolder);

        Long generatedId = keyHolder.getKey().longValue();
        cartItem.setId(generatedId);
        return cartItem;
    }

    public CartItem findById(Long id) {
        SqlParameterSource namedParameter = new MapSqlParameterSource("id", id);

        try {
            return namedParameterJdbcTemplate.queryForObject(
                    "SELECT id, wish_item_id, name, unit_price, stock, quantity FROM cart_items WHERE id=:id",
                    namedParameter,
                    new BeanPropertyRowMapper<>(CartItem.class)
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public CartItem findByWishItemId(Long wishItemId){
        SqlParameterSource namedParameter = new MapSqlParameterSource("wishItemId", wishItemId);

        try {
            return namedParameterJdbcTemplate.queryForObject(
                    "SELECT id, wish_item_id, name, unit_price, stock, quantity FROM cart_items WHERE wish_item_id=:wishItemId",
                    namedParameter,
                    new BeanPropertyRowMapper<>(CartItem.class)
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<CartItem> findAll() {
        List<CartItem> cartItems = namedParameterJdbcTemplate.query(
                "SELECT id, wish_item_id, name, unit_price, stock, quantity FROM cart_items ORDER BY id",
                new BeanPropertyRowMapper<>(CartItem.class)
        );

        return cartItems;
    }

    public CartItem update(CartItem cartItem) {
        SqlParameterSource namedParameter = new BeanPropertySqlParameterSource(cartItem);

        namedParameterJdbcTemplate.update("UPDATE cart_items SET quantity = :quantity WHERE id = :id", namedParameter);

        return cartItem;
    }

    public boolean delete(Long id) {
        SqlParameterSource namedParameter = new MapSqlParameterSource("id", id);

        int deletedCount = namedParameterJdbcTemplate.update(
                "DELETE FROM cart_items WHERE id=:id",
                namedParameter
        );

        return deletedCount > 0;
    }
}
