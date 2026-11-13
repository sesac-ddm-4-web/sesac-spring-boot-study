package com.example.wishlist.cart.repository;

import com.example.wishlist.cart.domain.Cart;
import com.example.wishlist.cart.domain.CartItem;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
public class DatabaseCartRepository implements CartRepository {

    private static final long CART_ID = 1L;

    private static final String SELECT_ITEMS = """
            SELECT id, wishlist_item_id, name, unit_price, quantity, stock_limit
            FROM cart_item
            WHERE cart_id = :cartId
            ORDER BY id
            """;

    private static final String SELECT_LAST_ITEM_ID = "SELECT last_item_id FROM cart WHERE id = :cartId";

    private static final String DELETE_ITEMS = "DELETE FROM cart_item WHERE cart_id = :cartId";

    private static final String INSERT_ITEM = """
            INSERT INTO cart_item (id, cart_id, wishlist_item_id, name, unit_price, quantity, stock_limit)
            VALUES (:id, :cartId, :wishlistItemId, :name, :unitPrice, :quantity, :stockLimit)
            """;

    private static final String UPDATE_LAST_ITEM_ID = "UPDATE cart SET last_item_id = :lastItemId WHERE id = :cartId";

    private static final RowMapper<CartItem> ROW_MAPPER = (rs, rowNum) -> CartItem.restore(
        rs.getLong("id"),
        rs.getLong("wishlist_item_id"),
        rs.getString("name"),
        rs.getLong("unit_price"),
        rs.getInt("quantity"),
        rs.getObject("stock_limit", Integer.class)
    );

    private final NamedParameterJdbcTemplate template;

    public DatabaseCartRepository(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    @Override
    public Cart load() {
        MapSqlParameterSource params = new MapSqlParameterSource("cartId", CART_ID);
        List<CartItem> items = template.query(SELECT_ITEMS, params, ROW_MAPPER);
        Long lastItemId = template.queryForObject(SELECT_LAST_ITEM_ID, params, Long.class);
        return Cart.restore(items, lastItemId);
    }

    @Override
    public void save(Cart cart) {
        template.update(DELETE_ITEMS, new MapSqlParameterSource("cartId", CART_ID));

        List<CartItem> items = cart.getItems();
        if (!items.isEmpty()) {
            SqlParameterSource[] batch = items.stream()
                .map(this::toParams)
                .toArray(SqlParameterSource[]::new);
            template.batchUpdate(INSERT_ITEM, batch);
        }

        template.update(UPDATE_LAST_ITEM_ID, new MapSqlParameterSource()
            .addValue("lastItemId", cart.getLastItemId())
            .addValue("cartId", CART_ID));

    }

    private SqlParameterSource toParams(CartItem item) {
        return new MapSqlParameterSource()
            .addValue("id", item.getId())
            .addValue("cartId", CART_ID)
            .addValue("wishlistItemId", item.getWishlistItemId())
            .addValue("name", item.getName())
            .addValue("unitPrice", item.getUnitPrice())
            .addValue("quantity", item.getQuantity())
            .addValue("stockLimit", item.getStockLimit());
    }
}
