package com.example.demo.cart.repository;

import com.example.demo.cart.domain.Cart;
import com.example.demo.cart.domain.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("dev")
public class DatabaseCartRepository implements CartRepository{

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public DatabaseCartRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate){
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Cart find() {
        List<CartItem> items = namedParameterJdbcTemplate.query(
                "SELECT c.id, c.item_id, i.name, i.price, i.stock, c.quantity "
                        + "FROM cart_items c "
                        + "JOIN collection_items i ON i.id = c.item_id "
                        + "ORDER BY c.id",
                (rs, rowNum) -> CartItem.restore(
                        rs.getLong("id"),
                        rs.getLong("item_id"),
                        rs.getString("name"),
                        rs.getInt("price"),
                        rs.getObject("stock", Integer.class),
                        rs.getInt("quantity")
                )
        );

        return Cart.restore(items);
    }

    @Override
    public void save(Cart cart) {
        List<Long> ids = cart.getItems().stream()
                .map(CartItem::getId)
                .toList();

        // 장바구니에서 빼면 db에서도 제거
        if (ids.isEmpty()) {
            namedParameterJdbcTemplate.update(
                    "DELETE FROM cart_items",
                    new MapSqlParameterSource()
            );
        } else {
            namedParameterJdbcTemplate.update(
                    "DELETE FROM cart_items WHERE id NOT IN (:ids)",
                    new MapSqlParameterSource("ids", ids)
            );
        }

        // 남은 것은 없으면 넣고 있으면 수량만 고침
        for (CartItem cartItem : cart.getItems()) {
            SqlParameterSource namedParameter = new MapSqlParameterSource()
                    .addValue("id", cartItem.getId())
                    .addValue("itemId", cartItem.getItemId())
                    .addValue("quantity", cartItem.getQuantity());

            namedParameterJdbcTemplate.update(
                    "INSERT INTO cart_items (id, item_id, quantity) "
                            + "VALUES (:id, :itemId, :quantity) "
                            + "ON DUPLICATE KEY UPDATE quantity = :quantity",
                    namedParameter
            );
        }
    }
}
