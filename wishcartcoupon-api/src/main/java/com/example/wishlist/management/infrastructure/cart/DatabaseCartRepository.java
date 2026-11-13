package com.example.wishlist.management.infrastructure.cart;

import com.example.wishlist.management.domain.cart.Cart;
import com.example.wishlist.management.domain.cart.CartItem;
import com.example.wishlist.management.domain.cart.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
@Primary
public class DatabaseCartRepository implements CartRepository {

    private static final RowMapper<CartItem> ITEM_MAPPER = (rs, rowNum) -> CartItem.restore(
            rs.getLong("id"),
            rs.getLong("wish_id"),
            rs.getString("name"),
            rs.getInt("price"),
            rs.getObject("stock", Integer.class),
            rs.getInt("quantity"));

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public DatabaseCartRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Cart get() {
        List<CartItem> items = namedParameterJdbcTemplate.query(
                "SELECT * FROM cart_item ORDER BY id", ITEM_MAPPER);
        return Cart.restore(items);
    }

    @Override
    @Transactional
    public void save(Cart cart) {
        namedParameterJdbcTemplate.update("DELETE FROM cart_item", Map.of());

        for (CartItem item : cart.getItems()) {
            namedParameterJdbcTemplate.update(
                    "INSERT INTO cart_item (id, wish_id, name, price, stock, quantity) "
                            + "VALUES (:id, :wishId, :name, :price, :stock, :quantity)",
                    new MapSqlParameterSource()
                            .addValue("id", item.getId())
                            .addValue("wishId", item.getWishId())
                            .addValue("name", item.getName())
                            .addValue("price", item.getPrice())
                            .addValue("stock", item.getStock())
                            .addValue("quantity", item.getQuantity()));
        }
    }
}