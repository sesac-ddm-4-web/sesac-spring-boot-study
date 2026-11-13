package com.example.sesac_spring_boot_study.infrastructure;

import com.example.sesac_spring_boot_study.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
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

@Repository
public class DatabaseCartRepository implements CartRepository {
    //private Cart cart = new Cart();
    //public Cart getCart() {
        //return cart;
    //}

    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public DatabaseCartRepository (NamedParameterJdbcTemplate namedParameterJdbcTemplate){
        this.namedParameterJdbcTemplate=namedParameterJdbcTemplate;
    }

    public CartItem add(CartItem cartItem){
        KeyHolder keyHolder = new GeneratedKeyHolder();
        BeanPropertySqlParameterSource namedParameter = new BeanPropertySqlParameterSource(cartItem);

        namedParameterJdbcTemplate.update("INSERT INTO cart_items (product_id, name, price, stock, quantity) VALUES (:productId, :name, :price, :stock, :quantity)",namedParameter,keyHolder);

        Long generatedId = keyHolder.getKey().longValue();
        cartItem.setRowId(generatedId);
        return cartItem;

    }

    public List<CartItem> findAll(){
        List<CartItem> cartItems = namedParameterJdbcTemplate.query(
                "SELECT * FROM cart_items ORDER BY row_id",
                new BeanPropertyRowMapper<>(CartItem.class)
        );

        return cartItems;
    }

    public CartItem update(CartItem cartItem){
        SqlParameterSource namedParameter = new BeanPropertySqlParameterSource(cartItem);

        namedParameterJdbcTemplate.update("UPDATE cart_items SET quantity = :quantity WHERE row_id = :rowId", namedParameter);

        return cartItem;
    }



    public void delete(Long rowId){
        SqlParameterSource namedParameter = new MapSqlParameterSource("rowId", rowId);

        namedParameterJdbcTemplate.update(
                "DELETE FROM cart_items WHERE row_id = :rowId",
                namedParameter
        );

    }
}
